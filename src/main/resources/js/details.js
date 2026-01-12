
const createDetailsUI = ({
  detailsPanel,
  detailsClose,
  detailsTitle,
  detailsStatsEl,
  skillsListEl,
  dpsFormatter,
  getDetails,
}) => {
  const clamp01 = (v) => Math.max(0, Math.min(1, v));

  const formatNum = (v) => {
    const n = Number(v);
    if (!Number.isFinite(n)) return "-";
    return dpsFormatter.format(n);
  };

  const STATUS = [
    { label: "누적 피해량", getValue: (d) => formatNum(d?.totalDmg) },
    { label: "피해량 기여도", getValue: (d) => d?.percent ?? "-" },
    { label: "보스 막기비율", getValue: (d) => d?.parry ?? "-" },
    { label: "보스 회피비율", getValue: (d) => d?.eva ?? "-" },
    { label: "전투시간", getValue: (d) => d?.combatTime ?? "-" },
  ];

  const createStatView = (labelText) => {
    const statEl = document.createElement("div");
    statEl.className = "stat";

    const labelEl = document.createElement("p");
    labelEl.className = "label";
    labelEl.textContent = labelText;

    const valueEl = document.createElement("p");
    valueEl.className = "value";
    valueEl.textContent = "-";

    statEl.appendChild(labelEl);
    statEl.appendChild(valueEl);

    return { statEl, valueEl };
  };

  const statSlots = STATUS.map((def) => createStatView(def.label));
  statSlots.forEach((v) => detailsStatsEl.appendChild(v.statEl));

  const renderStats = (details) => {
    for (let i = 0; i < STATUS.length; i++) {
      statSlots[i].valueEl.textContent = STATUS[i].getValue(details);
    }
  };

  const createSkillView = () => {
    const rowEl = document.createElement("div");
    rowEl.className = "skillRow";

    const nameEl = document.createElement("div");
    nameEl.className = "cell name";

    const castEl = document.createElement("div");
    castEl.className = "cell cast right";

    const dmgEl = document.createElement("div");
    dmgEl.className = "cell dmg right";

    const dmgFillEl = document.createElement("div");
    dmgFillEl.className = "dmgFill";

    const dmgTextEl = document.createElement("div");
    dmgTextEl.className = "dmgText";

    dmgEl.appendChild(dmgFillEl);
    dmgEl.appendChild(dmgTextEl);

    rowEl.appendChild(nameEl);
    rowEl.appendChild(castEl);
    rowEl.appendChild(dmgEl);

    return { rowEl, nameEl, castEl, dmgFillEl, dmgTextEl };
  };

  const skillSlots = [];
  const ensureSkillSlots = (n) => {
    while (skillSlots.length < n) {
      const v = createSkillView();
      skillSlots.push(v);
      skillsListEl.appendChild(v.rowEl);
    }
  };

  const renderSkills = (details) => {
    const list = Array.isArray(details?.skills) ? details.skills : [];
    const sorted = [...list]
      .sort((a, b) => (Number(b?.dmg) || 0) - (Number(a?.dmg) || 0))
      .slice(0, 10);

    const totalAllDmg = Number(details?.totalDmg) || 0;
    const denom =
      totalAllDmg > 0
        ? totalAllDmg
        : sorted.reduce((acc, s) => acc + (Number(s?.dmg) || 0), 0) || 1;

    ensureSkillSlots(sorted.length);

    for (let i = 0; i < skillSlots.length; i++) {
      const view = skillSlots[i];
      const s = sorted[i];

      if (!s) {
        view.rowEl.style.display = "none";
        view.dmgFillEl.style.transform = "scaleX(0)";
        continue;
      }

      view.rowEl.style.display = "";

      const dmg = Number(s.dmg) || 0;
      const pct = (dmg / denom) * 100;
      const pctRounded = Math.round(pct);
      const barRatio = clamp01(pct / 100);

      view.nameEl.textContent = s.name ?? "";
      view.castEl.textContent = `${formatNum(s.time)} (${formatNum(s.crit)})`;
      view.dmgTextEl.textContent = `${formatNum(dmg)} (${pctRounded}%)`;
      view.dmgFillEl.style.transform = `scaleX(${barRatio})`;
    }
  };

  const render = (details, row) => {
    detailsTitle.textContent = `${String(row.name)} 상세내역`;
    renderStats(details);
    renderSkills(details);
  };

  const isOpen = () => detailsPanel.classList.contains("open");

  const open = async (row) => {
    const details = await getDetails(row);
    render(details, row);

    detailsPanel.classList.add("open");

    // 포커스 (키 이벤트 유입 도움)
    requestAnimationFrame(() => detailsPanel.focus?.());
  };

  const close = () => detailsPanel.classList.remove("open");

  detailsClose?.addEventListener("click", close);

  // ESC 닫기 (자바가 키를 먹는 환경이면 JS로 안 올 수 있음)
  document.addEventListener(
    "keydown",
    (e) => {
      if (e.isComposing) return;
      if (e.key !== "Escape") return;
      if (!isOpen()) return;
      e.preventDefault();
      close();
    },
    true
  );

  // (호스트 호출용) 자바가 ESC를 잡는다면 여기로 닫게 가능
  // => 이건 "백엔드/호스트 통신용"이라 window 사용 룰에 부합
  window.__closeDetailsFromHost = close;

  return { open, close, isOpen, render };
};
