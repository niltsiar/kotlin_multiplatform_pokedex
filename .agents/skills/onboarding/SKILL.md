---
name: onboarding
description: This skill should be used when designing first-run experience, user onboarding flows, and welcome screens. Use for progressive disclosure and user education.
---

## When to Use

Use this skill for:

- **Triggers**:
  - "design first-run experience"
  - "create onboarding flow"
  - "welcome screen"
  - "user education"
  - "progressive disclosure"
  - "permission request flow"
  - "feature introduction"
  - "app walkthrough"

- **Exclusions**:
  - General UI/UX design → Use `ui-ux-designer` skill
  - Navigation flows → Use user flow planning
  - Product requirements → Use Product Design Mode
  - Static help/documentation → Use Documentation Mode

## Decision Framework

Before designing onboarding, ask yourself:

1. **What is the onboarding goal?**
   - Feature discovery → Show dual-theme showcase (Material vs Unstyled)
   - Value proposition → Explain "compare design systems in real-time"
   - Permission requests → Explain benefit before asking
   - NEVER create tutorial-style onboarding (users skip it)

2. **When should onboarding appear?**
   - First launch only → Use SharedPreferences/DataStore flag to track completion
   - After dual-theme preference is set → NEVER show again (user already knows)
   - Contextual → Show first-run modal before Pokemon list loads
   - NEVER block Pokemon list loading for onboarding completion

3. **How do I measure success?**
   - Completion rate → Track users who finish vs skip
   - Time to first action → Measure engagement after onboarding
   - Feature adoption → Track theme switching usage (Material ↔ Unstyled tabs)
   - A/B test different flows to optimize

## Essential Workflows

### Workflow 1: Design New Onboarding Flow

**MANDATORY - READ ENTIRE FILE**: Before designing onboarding flow, you MUST read [`resources/onboarding-flow-template.md`](resources/onboarding-flow-template.md) for complete template and examples.

1. Define onboarding goals and success metrics
2. Identify 1-3 key features to showcase (maximum 3 steps)
   - **This project**: First-run modal → Explain dual-theme showcase → Explore Material tab → Switch to Unstyled tab → Compare
3. Create value propositions for each screen
4. Design progressive disclosure sequence (reveal complexity gradually)
5. Add skip option on every screen
6. Implement completion tracking (SharedPreferences/DataStore for onboarding completion flag)
7. Plan A/B testing variants

### Workflow 2: Optimize Existing Onboarding

1. Analyze current completion metrics
2. Identify drop-off points
3. Simplify copy and CTAs
4. Reduce step count (target ≤ 3 screens)
5. Improve visual clarity
6. Test with new variants
7. Measure impact on retention

### Workflow 3: Permission Request Onboarding

1. Explain why permission is needed (user benefit)
2. Provide clear CTA action ("Enable" vs generic "OK")
3. Include secondary option ("Not now")
4. Show consequence of declining (graceful degradation)
5. Re-prompt at natural context later
6. Track opt-in rates

## When to Show Onboarding? (Decision Tree)

```
User launches app
    ├─ First install?
    │   ├─ YES → Show onboarding modal
    │   └─ NO → Check dual-theme preference
    │       ├─ Preference already set? (user has switched themes)
    │       │   ├─ YES → NEVER show onboarding (user already knows)
    │       │   └─ NO → Show onboarding modal
    │       └─ User explicitly dismissed onboarding?
    │           ├─ YES → NEVER show again (respect user choice)
    │           └─ NO → Show onboarding modal
    └─ App update with new feature?
        ├─ YES → Version-gated onboarding for new feature only
        └─ NO → Follow first install logic
```

**This project implementation**:
- Check SharedPreferences/DataStore flag: `onboarding_completed`
- Check dual-theme preference: `theme_selection` (Material/Unstyled)
- If `onboarding_completed == true` OR `theme_selection != null` → Skip onboarding
- If both false → Show first-run modal explaining dual-theme showcase

## Critical Guardrails

| Rule | Why |
|------|-----|
| **Maximum 3 steps** | Users drop off after 3 screens. Keep it short. |
| **Skip always available** | Never force users through onboarding. Respect agency. |
| **Value on each screen** | Every screen must explain "why this matters to me." |
| **Progressive disclosure** | Reveal complexity gradually. Don't overwhelm. |
| **Measure everything** | Track completion rate, time, skip rate. Data-driven iteration. |

## Quick Reference

| Task | Action | Reference |
|------|--------|-----------|
| New onboarding flow | See `resources/onboarding-flow-template.md` | Template |
| Write CTAs | Use action verbs + benefit (e.g., "Enable notifications") | Copywriting |
| Test onboarding | A/B test with variants, measure completion rate | Metrics |
| Reduce drop-off | Check step count, simplify copy, add skip option | Optimization |
| Permission requests | Explain benefit + graceful decline | Permissions |

## Cross-References

| Document | Purpose |
|----------|---------|
| `docs/project/user_flow.md` | User journeys and flows |
| `docs/project/prd.md` | Product requirements and acceptance criteria |
| `docs/project/ui_ux.md` | UI/UX guidelines |
| `../ui-ux-designer/SKILL.md` | General UI/UX design principles |
| `resources/onboarding-flow-template.md` | Ready-to-use onboarding template |

## Success Metrics

Track these metrics for all onboarding flows:

- **Completion rate**: Target >70%
- **Time to complete**: Target <2 minutes
- **Skip rate**: Monitor for drop-off indicators
- **Retention impact**: Compare 7-day retention with vs without onboarding
- **Permission opt-in rate**: Track for permission request screens

## Copywriting Guidelines

**CTA Best Practices**:

- Use action verbs: "Start Exploring", "Compare Themes", "See Pokémon"
- Add user benefit: "Start Exploring → Browse 1,000+ Pokémon"
- Avoid generic: "OK", "Next", "Continue"
- Keep it short: 2-4 words max
- Secondary CTAs: "Skip", "Not now", "Maybe later"

**Screen Content**:

- Headline: Value proposition (5-10 words)
  - **This project**: "Your Pokémon Journey Starts Here", "Compare Design Systems Live"
- Body: Brief description (1-2 sentences)
  - **This project**: "Explore every Pokémon with dual-theme showcase (Material vs Unstyled)"
- Visual: Simple illustration or icon
- Avoid jargon: Use plain language

## Progressive Disclosure Principles

1. **Start simple**: Show core value first
2. **Add context**: Explain advanced features only when needed
3. **Use anchors**: Connect new info to familiar concepts
4. **Provide escape**: Skip or defer advanced topics
5. **Layer depth**: Essential → Important → Optional

## NEVER Do (Anti-Patterns)

| Anti-Pattern | Why It's Wrong | This Project Example |
|--------------|----------------|----------------------|
| **Show onboarding after dual-theme preference is set** | User already knows about the feature | NEVER show modal after user has switched Material ↔ Unstyled tabs |
| **Block Pokemon list loading for onboarding completion** | Delays time-to-value, frustrates users | Show modal AFTER Pokemon list is visible, not before |
| **Force onboarding without skip option** | Violates user agency, increases drop-off | Always provide "Skip" button in top-right corner |
| **Show onboarding on every app launch** | Annoying, wastes user time | Use SharedPreferences/DataStore flag to track completion |
| **Tutorial-style multi-step walkthrough** | Users skip it, doesn't teach effectively | Use single first-run modal explaining dual-theme showcase |

## A/B Testing Framework

Test these variants to optimize:

- **Step count**: 2 vs 3 vs 4 screens
- **Copy**: Short vs detailed descriptions
- **Visuals**: Illustrations vs screenshots vs icons only
- **CTA placement**: Bottom right vs center vs top
- **Skip visibility**: Always visible vs fade in after 5s
- **Animation**: With vs without motion

Measure impact on:
- Completion rate
- Time to complete
- 7-day retention
- Feature engagement (theme switching usage)
