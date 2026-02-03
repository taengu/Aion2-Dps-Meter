#!/usr/bin/env python3
"""Probe Aion2 DPS payloads for alternative parse layouts.

Usage:
  python scripts/payload_probe.py < log.txt
  python scripts/payload_probe.py --file log.txt

The script scans DEBUG lines that include "payload=" and attempts to parse
varint fields to recover target/actor IDs and subsequent fields. It tries
multiple offsets and optional skips to surface candidate layouts.
"""
from __future__ import annotations

import argparse
import re
from dataclasses import dataclass
from typing import Iterable, Iterator, List, Optional, Sequence, Tuple

LOG_RE = re.compile(
    r"target (?P<target>\d+), actor (?P<actor>\d+), damage (?P<damage>\d+).*payload=(?P<payload>[0-9A-Fa-f ]+)"
)


@dataclass(frozen=True)
class VarInt:
    value: int
    length: int


def read_varint(data: bytes, offset: int = 0) -> Optional[VarInt]:
    value = 0
    shift = 0
    count = 0
    while True:
        if offset + count >= len(data):
            return None
        byte_val = data[offset + count]
        count += 1
        value |= (byte_val & 0x7F) << shift
        if (byte_val & 0x80) == 0:
            return VarInt(value=value, length=count)
        shift += 7
        if shift >= 32:
            return None


def parse_hex_payload(payload: str) -> bytes:
    cleaned = payload.strip().replace(" ", "")
    return bytes.fromhex(cleaned)


def iter_payloads(lines: Iterable[str]) -> Iterator[Tuple[int, int, int, bytes]]:
    for line in lines:
        match = LOG_RE.search(line)
        if not match:
            continue
        target = int(match.group("target"))
        actor = int(match.group("actor"))
        damage = int(match.group("damage"))
        payload_hex = match.group("payload")
        yield target, actor, damage, parse_hex_payload(payload_hex)


def decode_u32_le(data: bytes, offset: int) -> Optional[int]:
    if offset + 4 > len(data):
        return None
    return int.from_bytes(data[offset : offset + 4], "little")


def format_slice(data: bytes, start: int, length: int = 12) -> str:
    end = min(len(data), start + length)
    return " ".join(f"{b:02X}" for b in data[start:end])


def probe_payload(
    payload: bytes,
    target: int,
    actor: int,
    max_skip: int,
) -> List[str]:
    results: List[str] = []

    for start in range(len(payload)):
        target_info = read_varint(payload, start)
        if not target_info or target_info.value != target:
            continue
        after_target = start + target_info.length

        for skip in range(max_skip + 1):
            actor_offset = after_target + skip
            actor_info = read_varint(payload, actor_offset)
            if not actor_info or actor_info.value != actor:
                continue

            cursor = actor_offset + actor_info.length
            unknown_info = read_varint(payload, cursor)
            if not unknown_info:
                continue
            cursor += unknown_info.length

            skill_raw = decode_u32_le(payload, cursor)
            if skill_raw is None:
                continue
            cursor += 4

            damage_info = read_varint(payload, cursor)
            if not damage_info:
                continue
            cursor += damage_info.length

            candidate = (
                f"target@{start}({target_info.length}b) skip={skip} actor@{actor_offset}({actor_info.length}b) "
                f"unknown={unknown_info.value} skill_raw={skill_raw} skill_code={skill_raw // 100} "
                f"damage={damage_info.value} next@{cursor}"
            )
            results.append(candidate)

    return results


def summarize_varints(payload: bytes, limit: int = 12) -> str:
    vals: List[str] = []
    cursor = 0
    for _ in range(limit):
        info = read_varint(payload, cursor)
        if not info:
            break
        vals.append(f"{info.value}({info.length}b)")
        cursor += info.length
        if cursor >= len(payload):
            break
    return ", ".join(vals)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--file", type=str, help="Read logs from a file instead of stdin.")
    parser.add_argument("--max-skip", type=int, default=3, help="Max bytes to skip between target and actor.")
    parser.add_argument("--show-raw", action="store_true", help="Print raw payload bytes.")
    args = parser.parse_args()

    if args.file:
        with open(args.file, "r", encoding="utf-8") as handle:
            lines = list(handle)
    else:
        import sys

        lines = sys.stdin.readlines()

    any_found = False
    for idx, (target, actor, damage, payload) in enumerate(iter_payloads(lines), start=1):
        any_found = True
        print(f"\n== Payload {idx} ==")
        print(f"target={target} actor={actor} damage={damage} length={len(payload)}")
        if args.show_raw:
            print("raw:", " ".join(f"{b:02X}" for b in payload))
        print("varints@0:", summarize_varints(payload))
        print("head bytes:", format_slice(payload, 0))

        candidates = probe_payload(payload, target, actor, args.max_skip)
        if candidates:
            print("candidates:")
            for entry in candidates:
                print("  -", entry)
        else:
            print("candidates: none")
            # fall back: scan for actor/target varints anywhere
            print("scan: first matches for target/actor")
            for label, needle in ("target", target), ("actor", actor):
                positions: List[str] = []
                for offset in range(len(payload)):
                    info = read_varint(payload, offset)
                    if info and info.value == needle:
                        positions.append(f"{offset}({info.length}b)")
                if positions:
                    print(f"  {label}: {', '.join(positions)}")
                else:
                    print(f"  {label}: none")

    if not any_found:
        print("No payload lines found. Expected lines with 'payload=' and target/actor/damage info.")
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
