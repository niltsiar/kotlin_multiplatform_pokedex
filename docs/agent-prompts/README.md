# Agents and Prompts Index (Canonical)

Last Updated: December 20, 2025

Rule: prompts MUST be link-first. Do not duplicate shared rules from `AGENTS.md` / `conventions.md` / testing guides.
Role prompts MUST be deltas and assume the consumer includes:

- [base_agent_prompt.md](base_agent_prompt.md)

## Specialized Agents (Pick One)

- Product Design Mode
  - Purpose: Derive features, acceptance criteria, scope from PRD; plan MVP and iterations.
  - Use when: You need to clarify product requirements from [prd.md](../project/prd.md).
  - Entry: [product_designer_agent_system_prompt_DELTA.md](product_designer_agent_system_prompt_DELTA.md)

- UI/UX Design Mode (Compose-first)
  - Purpose: Design layouts, interactions, motion for Compose Multiplatform.
  - Use when: Creating or refining Compose screens/animations.
  - Entry: [uiux_agent_system_prompt_DELTA.md](uiux_agent_system_prompt_DELTA.md)

- Screen Implementation Mode (Compose UI builder)
  - Purpose: Implement a concrete screen from specs with previews.
  - Use when: You have a screen spec and need end-to-end Compose implementation.
  - Entry: [ui_ux_system_agent_for_generic_screen_DELTA.md](ui_ux_system_agent_for_generic_screen_DELTA.md)

- SwiftUI Screen Agent (iOS production UI)
  - Purpose: Design/implement SwiftUI screens backed by KMP ViewModels via `shared.framework`.
  - Use when: Building native iOS UI.
  - Entry: [ui_ux_system_agent_for_swiftui_screen_DELTA.md](ui_ux_system_agent_for_swiftui_screen_DELTA.md)

- KMP Mobile Expert Mode
  - Purpose: Shared KMP business logic (ViewModels, repositories, modules, iOS bridging).
  - Use when: Implementing shared layer features, architecture decisions, iOS export questions.
  - Entry: [kmp_mobile_expert_agent_system_prompt_DELTA.md](kmp_mobile_expert_agent_system_prompt_DELTA.md)

- Onboarding Design Mode
  - Purpose: Craft onboarding flows and copy.
  - Use when: First-run, education, account creation flows.
  - Entry: [onboarding_agent_system_prompt_DELTA.md](onboarding_agent_system_prompt_DELTA.md)

- User Flow Planning Mode
  - Purpose: Map end-to-end journeys and navigation contracts.
  - Use when: Planning flows across screens and states.
  - Entry: [user_flow_agent_system_prompt_DELTA.md](user_flow_agent_system_prompt_DELTA.md)

- Testing Strategy Mode
  - Purpose: Define/upgrade tests (Kotest, MockK, Turbine, properties) to meet enforcement.
  - Use when: Planning coverage or improving reliability.
  - Entry: [testing_agent_system_prompt_DELTA.md](testing_agent_system_prompt_DELTA.md)

- Backend Development Mode
  - Purpose: Design/implement Ktor BFF endpoints and DTOs.
  - Use when: Server-side changes.
  - Entry: [backend_agent_system_prompt_DELTA.md](backend_agent_system_prompt_DELTA.md)

- Documentation Management Mode
  - Purpose: Keep `AGENTS.md`, `.junie/guidelines.md`, and `.github/copilot-instructions.md` aligned with canonicals in `docs/**`.
  - Use when: Documentation audits and migrations.
  - Entry: [documentation_agent_system_prompt_DELTA.md](documentation_agent_system_prompt_DELTA.md)

## Supporting Docs

- Animation examples (SwiftUI): [animation_example_guides_swiftui.md](animation_example_guides_swiftui.md)
- Animation examples (Compose): [animation_example_guides.md](animation_example_guides.md)
- Easter eggs / mini-games guide: [easter_eggs_and_mini_games_guide.md](easter_eggs_and_mini_games_guide.md)

Tip: If unsure which to choose, open [AGENTS.md](../../AGENTS.md) and jump to the “Task Type Decision Tree”.
