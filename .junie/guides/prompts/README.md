# Agents and Prompts Index

Last Updated: November 27, 2025

This folder contains the specialized agent prompts. Use this index to pick the right mode quickly.

- Product Design Mode
  - Purpose: Derive features, acceptance criteria, scope from PRD; plan MVP and iterations.
  - Use when: You need to clarify product requirements from `.junie/guides/project/prd.md`.
  - Entry: `product_designer_agent_system_prompt.md`

- UI/UX Design Mode (Compose-first)
  - Purpose: Design layouts, interactions, motion for Compose Multiplatform.
  - Use when: Creating or refining Compose screens/animations.
  - Entry: `uiux_agent_system_prompt.md`

- Screen Implementation Mode (Compose UI builder)
  - Purpose: Implement a concrete screen from specs with previews.
  - Use when: You have a screen spec and need end-to-end Compose implementation.
  - Entry: `ui_ux_system_agent_for_generic_screen.md`

- SwiftUI Screen Agent (iOS production UI)
  - Purpose: Design/implement SwiftUI screens backed by KMP ViewModels via `shared.framework`.
  - Use when: Building native iOS UI.
  - Entry: `ui_ux_system_agent_for_swiftui_screen.md`

- Onboarding Design Mode
  - Purpose: Craft onboarding flows and copy.
  - Use when: First-run, education, account creation flows.
  - Entry: `onboarding_agent_system_prompt.md`

- User Flow Planning Mode
  - Purpose: Map end-to-end journeys and navigation contracts.
  - Use when: Planning flows across screens and states.
  - Entry: `user_flow_agent_system_prompt.md`

- Testing Strategy Mode
  - Purpose: Define/upgrade tests (Kotest, MockK, Turbine, properties) to meet enforcement.
  - Use when: Planning coverage or improving reliability.
  - Entry: `testing_agent_system_prompt.md`

- Backend Development Mode
  - Purpose: Design/implement Ktor BFF endpoints and DTOs.
  - Use when: Server-side changes.
  - Entry: `backend_agent_system_prompt.md`

- Documentation Management Mode
  - Purpose: Keep `AGENTS.md`, `.junie/guidelines.md`, and `.github/copilot-instructions.md` in sync.
  - Use when: Documentation audits and updates.
  - Entry: `documentation_agent_system_prompt.md`

- Reference (supporting docs)
  - Animations examples (SwiftUI): `animation_example_guides_swiftui.md`

Tip: If unsure which to choose, open `AGENTS.md` and jump to the “Task Type Decision Tree”.
