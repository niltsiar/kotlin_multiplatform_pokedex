# Onboarding Flow Template

Use this template to design effective onboarding flows. Replace bracketed content with your specific app details.

---

## Step 1: Welcome

**Headline**: [Value proposition - what users get]
> Example: "Explore All Pokémon Stats"

**Body**: [Brief description - 1-2 sentences]
> Example: "Browse complete Pokémon data with stats, types, and abilities."

**Visual**: [Illustration or icon]
> Example: App logo + welcome illustration

**CTA**: "Get Started" [Primary button, bottom right]

**Secondary**: "Skip" [Text link, top right or bottom left]

**Success Metric**: Time to complete <30 seconds

---

## Step 2: [Feature/Permission] - Optional

**Headline**: [Benefit - why user should enable]
> Example: "Stay Updated"

**Body**: [Why we need this + user benefit]
> Example: "Get notified about new Pokémon features. We respect your privacy."

**Visual**: [Contextual illustration or diagram]
> Example: Bell icon + notification preview mockup

**CTA**: "Enable" [Primary button]

**Secondary**: "Not now" [Text link]

**Note**: Graceful degradation if user declines
> Example: App works without notifications, feature available in settings later

**Success Metric**: Opt-in rate >40%

---

## Step 3: Complete

**Headline**: "You're all set!"

**Body**: [What users can do now]
> Example: "Start exploring Pokémon. Tap any card to see detailed stats."

**Visual**: [Celebratory illustration or success state]
> Example: Confetti animation + app screen preview

**CTA**: "Start Exploring" [Primary button]

**Secondary**: "Skip to list" [Text link - optional]

**Success Metric**: Overall completion rate >70%

---

## Success Metrics

Track these metrics for the complete onboarding flow:

| Metric | Target | How to Track |
|--------|--------|--------------|
| Completion rate | >70% | Users who finish all steps / users who start |
| Time to complete | <2 minutes | Timestamp start to finish |
| Skip rate | <30% | Users who skip / total users |
| Step 2 opt-in | >40% | Users who enable feature / users who see step 2 |
| 7-day retention | Baseline +10% | Compare users with vs without onboarding |
| Feature engagement | Baseline +15% | Feature usage with vs without onboarding |

---

## Progressive Disclosure Checklist

- [ ] Step 1 establishes core value immediately
- [ ] Step 2 explains one feature/permission only
- [ ] Step 3 celebrates completion and guides to main app
- [ ] Skip option available on all screens
- [ ] No jargon or technical terms
- [ ] Clear benefit on each screen ("why this matters to me")
- [ ] Visuals support (not distract from) content
- [ ] CTAs use action verbs ("Get Started" vs "OK")
- [ ] Total time to complete <2 minutes
- [ ] Graceful degradation for declined permissions

---

## A/B Testing Variations

Test these to optimize your onboarding:

**Step Count**:
- Variant A: 2 steps (Welcome → Complete)
- Variant B: 3 steps (Welcome → Feature → Complete)
- Variant C: 4 steps (add another feature)

**Copy Length**:
- Variant A: Minimal (headline + 1 sentence)
- Variant B: Balanced (headline + 2 sentences)
- Variant C: Detailed (headline + 3 sentences)

**CTA Placement**:
- Variant A: Bottom right only
- Variant B: Centered below content
- Variant C: Bottom right + top progress indicator

**Skip Visibility**:
- Variant A: Always visible (top right)
- Variant B: Fade in after 5 seconds
- Variant C: Only on last screen

**Animation**:
- Variant A: With motion (slide transitions, subtle animations)
- Variant B: Without motion (instant screen changes)

Measure impact on: completion rate, time to complete, retention.

---

## Implementation Notes

### State Management

Track onboarding state persistently:
```kotlin
data class OnboardingState(
    isCompleted: Boolean,
    stepIndex: Int, // 0 = not started, 1-3 = current step
    skipped: Boolean,
    permissionsOptedIn: Map<Permission, Boolean>
)
```

### Skip Behavior

- User skips → Mark onboarding as completed
- Don't show onboarding again on subsequent app launches
- Provide "Show onboarding again" option in settings (optional)

### Re-trigger Onboarding

Re-show onboarding when:
- Major app version update (e.g., 1.0 → 2.0)
- User explicitly requests in settings
- A/B testing requires fresh cohort

### Analytics Events

Track these events:
- `onboarding_started`
- `onboarding_step_{n}_viewed`
- `onboarding_step_{n}_completed`
- `onboarding_skipped`
- `onboarding_completed`
- `permission_requested_{permission}`
- `permission_granted_{permission}`
- `permission_denied_{permission}`
