---
name: product-designer
description: This skill should be used when writing product requirements, defining scope, and planning features. Use for PRD creation, acceptance criteria, and MVP planning.
---

## When to Use

Use this skill when:

**Trigger Scenarios:**
- Creating or updating Product Requirements Documents (PRDs)
- Defining feature scope and boundaries (MVP vs future enhancements)
- Writing acceptance criteria with Gherkin format (Given/When/Then)
- Structuring user stories for development teams
- Planning feature prioritization and roadmaps
- Documenting success metrics and validation criteria
- Scoping work for cross-platform implementations

**Exclusion Scenarios (DO NOT use):**
- Implementation details (use technical implementation skills)
- UI/UX visual design specifications (use frontend-ui-ux skill)
- Test planning strategies (use testing-strategy skill)
- Code architecture decisions (use technical patterns documentation)
- Backend API specifications (use backend-development skill)

## Essential Workflows

### Workflow 1: Create a Complete PRD

To create a comprehensive Product Requirements Document:

1. **Define the Problem Statement**
   - Describe the user pain point clearly
   - Quantify the problem (metrics if available)
   - Explain why this matters now

2. **Propose the Solution Overview**
   - High-level description of the proposed solution
   - Key features and capabilities
   - Technical approach (brief)

3. **Establish Scope Boundaries**
   - List features in MVP (Minimum Viable Product)
   - List features deferred to v2+ (Out of Scope)
   - Explicitly state what will NOT be built

4. **Define Success Metrics**
   - Quantitative metrics (e.g., "load time <2s", "85% adoption rate")
   - Qualitative metrics (e.g., "users report smoother workflow")
   - How metrics will be measured

5. **Write Acceptance Criteria**
   - Use Gherkin format: Given [context], When [action], Then [expected result]
   - Cover happy paths and edge cases
   - Make criteria testable and unambiguous

6. **Document User Stories**
   - Format: "As a [user], I want [goal], so that [benefit]"
   - Include one story per major feature
   - Prioritize by user value

### Workflow 2: Define MVP vs Future Scope

To separate MVP from future enhancements:

1. **Identify Core Value Proposition**
   - What is the ONE thing users need?
   - What is the minimum viable solution?
   - What can be simplified or omitted?

2. **Apply the "Must Have vs Nice to Have" Framework**
   - **Must Have (MVP)**: Critical path to value, cannot ship without
   - **Should Have (v2)**: Important but can be added later
   - **Could Have (v3)**: Desirable but low priority
   - **Won't Have (Out of Scope)**: Explicitly excluded

3. **Create a Feature Prioritization Matrix**
   - Score each feature on: User Value (1-5), Technical Complexity (1-5), Effort (1-5)
   - Calculate: Priority Score = (User Value × 2) - Technical Complexity - Effort
   - Sort by Priority Score (highest first)

4. **Document Rationale**
   - Explain why each MVP feature was included
   - Explain why each v2 feature was deferred
   - Link back to problem statement

### Workflow 3: Write Testable Acceptance Criteria

To create clear, testable acceptance criteria:

1. **Structure Each Criterion with Gherkin**
   - **Given**: Precondition/context (e.g., "Given the user is on the home screen")
   - **When**: Action taken by user or system (e.g., "When the user taps the search button")
   - **Then**: Expected result (e.g., "Then a search bar appears at the top of the screen")

2. **Cover Multiple Scenarios**
   - **Happy Path**: Expected behavior under normal conditions
   - **Error Cases**: Network failure, invalid input, timeouts
   - **Edge Cases**: Empty states, boundary conditions, concurrent actions

3. **Make Criteria Measurable**
   - Avoid ambiguous words like "quickly", "smoothly", "intuitive"
   - Use specific numbers or thresholds: "loads within 2 seconds", "displays 10 items per page"
   - Tie to success metrics when possible

4. **Organize by Feature or User Story**
   - Group criteria logically (e.g., "Search Feature", "Navigation", "Data Loading")
   - Number each criterion for easy reference during testing
   - Reference user stories in cross-refs

## Critical Guardrails

| Rule | Description | Rationale |
|------|-------------|-----------|
| **One Problem Per PRD** | Focus each PRD on a single user problem or feature set | Prevents scope creep, keeps documents actionable |
| **Testable Acceptance Criteria** | Every criterion must be verifiable by QA or automated tests | Unclear criteria lead to misaligned expectations |
| **Explicit Out of Scope** | List what is NOT being built (not just v2 features) | Prevents debates about "did we forget X?" |
| **Quantify Success Metrics** | Use numbers and measurement methods, not qualitative statements | Enables objective validation and post-launch analysis |
| **Reference Existing Patterns** | Link to architecture docs, conventions, and existing PRDs | Avoids reinventing the wheel, maintains consistency |

## Quick Reference

| Template | When to Use | Format |
|----------|-------------|--------|
| **PRD Structure** | Creating a new product requirement document | Problem → Solution → Scope → Success Metrics → Acceptance Criteria |
| **User Story** | Describing feature from user perspective | "As a [user], I want [goal], so that [benefit]" |
| **Acceptance Criterion** | Defining testable behavior | Given [context], When [action], Then [expected result] |
| **MVP Definition** | Scoping minimum viable product | Core value features only, defer all enhancements to v2+ |
| **Success Metric** | Measuring feature success | Metric name + target value + measurement method |

## Cross-References

| Document | Purpose | Location |
|----------|---------|----------|
| `prd.md` | Example complete PRD for this project | `docs/project/prd.md` |
| `user_flow.md` | Example user flows and screen definitions | `docs/project/user_flow.md` |
| `conventions.md` | Architecture and technical constraints | `docs/tech/conventions.md` |
| `critical_patterns_quick_ref.md` | Technical patterns that influence scope | `docs/tech/critical_patterns_quick_ref.md` |
| `prd-template.md` | Ready-to-use PRD template resource | `.claude/skills/product-designer/resources/prd-template.md` |

## Pro Tips

1. **Start with the Problem, Not the Solution** — If you can't articulate the problem clearly, the PRD will be unfocused
2. **Write PRDs for Humans, Not Machines** — Use clear, conversational language that developers can understand
3. **Iterate on Acceptance Criteria** — Review criteria with engineers and QA before finalizing
4. **Version Your PRDs** — Add "Last Updated" dates and track changes for accountability
5. **Link to Implementation Details** — Reference technical docs (navigation, conventions) to connect product to code
