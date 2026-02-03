meta:
  id: aion2_damage_packet
  title: AION2 damage packet (basic)
  endian: le
  file-extension: bin
doc: |
  Describes the core structure parsed by StreamProcessor.DamagePacketReader for
  non-DoT damage packets (opcode 0x04 0x38).
  Varints are unsigned LEB128-style (readVarInt in StreamProcessor).
seq:
  - id: length
    type: uleb128
    doc: Packet length varint. StreamProcessor uses this for size checks.
  - id: opcode
    contents: [0x04, 0x38]
    doc: Damage packet opcode.
  - id: target_id
    type: uleb128
  - id: switch_info
    type: uleb128
    doc: Lower 4 bits select special flag block size.
  - id: flag_info
    type: uleb128
  - id: actor_id
    type: uleb128
  - id: skill_id
    type: uleb128
    doc: |
      Skill id is parsed as a varint. Caller validates a 2-4 byte range and
      may backtrack +/- 1 byte to resync.
  - id: attack_uid
    type: u4
    doc: |
      Per-attack UID (u32le). Values in ~2.5M..4M are treated as attack UIDs.
  - id: effect_marker
    type: effect_marker
    if: _io.size >= _root._io.pos + 2
  - id: type_info
    type: uleb128
  - id: damage_type
    type: u1
  - id: special_flags
    type: bytes
    size: special_flag_len
  - id: hit_count
    type: uleb128
  - id: hit_values
    type: uleb128
    repeat: expr
    repeat-expr: hit_count
    doc: |
      Per-hit varints. The parser selects the final damage using tail scanning
      heuristics (largest sane or sum-matching).
types:
  effect_marker:
    seq:
      - id: marker
        size: 2
        doc: |
          Optional 0x01 0x03 or 0x01 0x10 marker; used to detect effect damage.
instances:
  switch_value:
    value: switch_info & 0x0f
  special_flag_len:
    value: |
      switch_value == 4 ? 8 :
      switch_value == 5 ? 12 :
      switch_value == 6 ? 10 :
      switch_value == 7 ? 14 : 0
