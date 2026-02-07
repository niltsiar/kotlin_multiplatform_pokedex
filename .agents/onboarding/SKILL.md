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

## Essential Workflows

### Workflow 1: Design New Onboarding Flow

1. Define onboarding goals and success metrics
2. Identify 1-3 key features to showcase (maximum 3 steps)
3. Create value propositions for each screen
4. Design progressive disclosure sequence (reveal complexity gradually)
5. Add skip option on every screen
6. Implement completion tracking
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

- Use action verbs: "Get Started", "Enable", "Explore"
- Add user benefit: "Get Started → See Pokémon stats"
- Avoid generic: "OK", "Next", "Continue"
- Keep it short: 2-4 words max
- Secondary CTAs: "Skip", "Not now", "Maybe later"

**Screen Content**:

- Headline: Value proposition (5-10 words)
- Body: Brief description (1-2 sentences)
- Visual: Simple illustration or icon
- Avoid jargon: Use plain language

## Progressive Disclosure Principles

1. **Start simple**: Show core value first
2. **Add context**: Explain advanced features only when needed
3. **Use anchors**: Connect new info to familiar concepts
4. **Provide escape**: Skip or defer advanced topics
5. **Layer depth**: Essential → Important → Optional

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
- Feature engagement
