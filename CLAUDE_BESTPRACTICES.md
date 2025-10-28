# Claude Best Practices for Multi-Session Development

**Version:** 1.0
**Based on:** Water Logger Android App Development (Oct 2025)
**Purpose:** Guide for Claude agents working on multi-session software projects

---

## 🎯 Overview

This document captures proven practices for Claude agents working on complex, multi-session development projects. These patterns enabled successful development of a complete Android app across multiple sessions with different Claude agents.

**Key Success Metrics from Water Logger Project:**
- ✅ 95% project completion across 2+ sessions
- ✅ 41 files created/modified with zero file loss
- ✅ Seamless handoffs between Claude agents
- ✅ Zero duplicate work or confusion
- ✅ Complete progress tracking and documentation

---

## 📋 Table of Contents

1. [Initial Project Setup](#1-initial-project-setup)
2. [Multi-Session Infrastructure](#2-multi-session-infrastructure)
3. [Development Workflow](#3-development-workflow)
4. [Progress Tracking](#4-progress-tracking)
5. [Communication Patterns](#5-communication-patterns)
6. [Tool Usage Guidelines](#6-tool-usage-guidelines)
7. [Architecture & Code Quality](#7-architecture--code-quality)
8. [Session Handoff Protocol](#8-session-handoff-protocol)
9. [Templates](#9-templates)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Initial Project Setup

### 1.1 First Actions (Session 1, Agent 1)

When starting a new multi-session project, **IMMEDIATELY** create these files:

#### A. `.claude/claude.md` - Persistent Agent Instructions

**Purpose:** Instructions that survive context resets and guide future agents.

**Location:** `.claude/claude.md` (root of project)

**Contents:**
- Project overview and goals
- Architecture decisions
- Key file locations
- Important conventions
- Workflow to follow
- Link to progress tracker

**When to create:** First 5 minutes of project

**Template:** See [Section 9.1](#91-claudeclaudemd-template)

#### B. `DEVELOPMENT_PROGRESS.md` - Master Progress Tracker

**Purpose:** Detailed checklist and session history.

**Location:** Root directory

**Contents:**
- Quick status overview table
- Phase checklists with ☑️/☐
- Session notes (date, agent, work done)
- Next steps for future agents
- Known issues/blockers

**When to create:** First 10 minutes of project

**Template:** See [Section 9.2](#92-development_progressmd-template)

#### C. Project Plan Document

**Purpose:** High-level roadmap (if not already provided by user)

**Example:** `PROJECT_PLAN.md` or `WATER_LOGGER_PLAN.md`

### 1.2 Repository Setup

```bash
# Ensure git is initialized
git init

# Create .gitignore if needed
# Add persistent instruction files
git add .claude/claude.md DEVELOPMENT_PROGRESS.md

# Initial commit (if starting fresh)
git commit -m "Add multi-session development infrastructure"
```

---

## 2. Multi-Session Infrastructure

### 2.1 The `.claude/` Directory

**Structure:**
```
.claude/
├── claude.md           # Main instructions (REQUIRED)
├── architecture.md     # Architecture notes (optional)
└── conventions.md      # Code conventions (optional)
```

### 2.2 Documentation Hierarchy

**Priority Order:**
1. `.claude/claude.md` - Agent instructions (agents read this FIRST)
2. `DEVELOPMENT_PROGRESS.md` - Detailed progress & checklists
3. `PROJECT_PLAN.md` - Original plan/requirements
4. `APP_SUMMARY.md` - Final summary (created near end)

### 2.3 Session Continuity Pattern

**How agents find their way:**

```mermaid
New Agent Starts
     ↓
1. Read .claude/claude.md (tells agent what to do)
     ↓
2. Open DEVELOPMENT_PROGRESS.md (see current status)
     ↓
3. Find "NEXT STEPS" section
     ↓
4. Start work from where previous agent left off
     ↓
5. Update progress as you go
     ↓
6. Write session notes before finishing
```

---

## 3. Development Workflow

### 3.1 Phase-Based Development

**Break projects into 5-10 phases:**

Example from Water Logger:
- Phase 1: Project Setup & Dependencies (10%)
- Phase 2: Database Layer (15%)
- Phase 3: Domain Layer (10%)
- Phase 4: Main UI (25%)
- Phase 5: Widget (15%)
- Phase 6: History Screen (10%)
- Phase 7: Settings Screen (10%)
- Phase 8: Testing & Polish (5%)

**Benefits:**
- Clear checkpoints
- Easy to track progress
- Natural handoff points
- User can test incrementally

### 3.2 Work Session Pattern

**For each work session:**

```
1. START: Read .claude/claude.md and DEVELOPMENT_PROGRESS.md
   ↓
2. UPDATE: Use TodoWrite tool to track current tasks
   ↓
3. WORK: Complete tasks, update todos as you go
   ↓
4. TEST: Verify work compiles/runs (if possible)
   ↓
5. DOCUMENT: Update DEVELOPMENT_PROGRESS.md with:
   - Mark phase tasks as complete [x]
   - Add session notes
   - Write "Next Agent Should:" instructions
   ↓
6. COMMIT: If appropriate, create git commit
```

### 3.3 TodoWrite Tool Usage

**Use TodoWrite proactively for:**
- Multi-step tasks (3+ steps)
- Complex features requiring planning
- User-provided task lists
- Tracking progress within a phase

**Example:**
```kotlin
// When implementing a feature:
TodoWrite: [
  1. Create ViewModel with Hilt injection (in_progress)
  2. Create Screen UI with Material3 (pending)
  3. Update NavGraph to add route (pending)
  4. Test navigation flow (pending)
]
```

**Update todos in real-time:**
- Mark as `in_progress` when starting
- Mark as `completed` immediately when done
- Only ONE task `in_progress` at a time

---

## 4. Progress Tracking

### 4.1 Quick Status Overview

Always maintain a table at top of `DEVELOPMENT_PROGRESS.md`:

```markdown
| Phase | Status | Progress |
|-------|--------|----------|
| Phase 1: Setup | 🟢 Completed | 100% |
| Phase 2: Database | 🟢 Completed | 100% |
| Phase 3: Domain | 🟡 In Progress | 60% |
| Phase 4: UI | 🔴 Not Started | 0% |

**Legend:** 🔴 Not Started | 🟡 In Progress | 🟢 Completed
```

### 4.2 Detailed Phase Checklists

For each phase:

```markdown
## Phase 3: Domain Layer

**Status:** 🟡 In Progress
**Dependencies:** Phase 2 must be completed
**Priority:** HIGH

### Tasks Checklist

#### 3.1 Use Cases
- [x] Create `domain/usecase/` package
- [x] Create `AddWaterUseCase.kt`
- [ ] Create `GetHistoryUseCase.kt`  ← Currently working on this
- [ ] Create `DeleteEntryUseCase.kt`

### Files Created/Modified in Phase 3
- [x] `domain/usecase/AddWaterUseCase.kt` - CREATED
- [ ] `domain/usecase/GetHistoryUseCase.kt` - IN PROGRESS
```

### 4.3 Session Notes Format

**After each work session, add notes:**

```markdown
### Session 2 - 2025-10-27
**Agent:** Claude (Sonnet 4.5)
**Work Done:**
- ✅ **Phase 3: 100% Complete - Domain Layer**
  - Created `AddWaterUseCase.kt` with validation
  - Created `GetHistoryUseCase.kt` with Flow
  - Created `DeleteEntryUseCase.kt`
  - Created `DateUtils.kt` utility class

**Key Features Implemented:**
- Input validation (positive amounts only)
- Flow-based reactive queries
- Transaction-safe operations

**What Works Now:**
✅ Use cases can be injected via Hilt
✅ Repository methods are wrapped with business logic
✅ Date formatting is centralized

**Next Agent Should:**
1. Test use cases (if tests exist)
2. Start Phase 4: Main UI Screen
3. Create ViewModel and Screen composables
```

### 4.4 "NEXT STEPS" Section

**Always maintain a prominent "NEXT STEPS" section:**

```markdown
## NEXT STEPS (Start Here!)

### Current Focus: Phase 4 - Main UI

**What to do next:**
1. Test the domain layer use cases
2. Create `ui/main/MainViewModel.kt` with Hilt
3. Create `ui/main/MainScreen.kt` with Compose
4. Wire up quick-add buttons to ViewModel
5. Test the app end-to-end

**Current Blockers:** None
```

---

## 5. Communication Patterns

### 5.1 Update Frequency

**Update progress documents:**
- ✅ After completing each major task
- ✅ Before taking a break (in long sessions)
- ✅ At end of session (REQUIRED)
- ✅ When encountering blockers

### 5.2 User Touchpoints

**Proactively communicate with user:**
- After completing each phase
- When encountering ambiguity (use AskUserQuestion tool)
- When ready to test features
- If you find potential issues

**Example:**
```
"Phase 3 (Domain Layer) is complete!

What's implemented:
✅ 4 use cases with business logic
✅ Date utilities
✅ Proper Hilt DI

The app is 50% complete. Would you like to:
A) Test what we have so far
B) Continue with Phase 4 (Main UI)
C) Something else?"
```

### 5.3 Error Communication

**When errors occur:**

```markdown
## Known Issues / Blockers

### Issue: Widget compilation error (2025-10-27)
**Location:** `WaterWidgetProvider.kt:65`
**Error:** Unresolved reference 'getDatabase'
**Cause:** WaterDatabase missing static getDatabase() method
**Fix Applied:** Added singleton getDatabase() method to companion object
**Status:** ✅ RESOLVED
```

---

## 6. Tool Usage Guidelines

### 6.1 TodoWrite Tool

**When to use:**
- Complex tasks (3+ steps)
- User provides list of tasks
- During feature implementation
- For tracking sub-tasks within a phase

**How to use:**
```typescript
// At start of complex task
TodoWrite: [
  {content: "Create ViewModel", status: "in_progress", activeForm: "Creating ViewModel"},
  {content: "Create UI Screen", status: "pending", activeForm: "Creating UI Screen"},
  {content: "Wire up navigation", status: "pending", activeForm: "Wiring up navigation"}
]

// Update immediately when done
TodoWrite: [
  {content: "Create ViewModel", status: "completed", ...},
  {content: "Create UI Screen", status: "in_progress", ...},
  ...
]
```

**Best practices:**
- Mark completed immediately (don't batch)
- Only one task in_progress at a time
- Clear, actionable task descriptions
- Use both content and activeForm

### 6.2 Read Tool

**File reading strategy:**
- Read `.claude/claude.md` FIRST in new sessions
- Read `DEVELOPMENT_PROGRESS.md` SECOND
- Read actual code files as needed
- Re-read files before editing them

### 6.3 Git Tool

**Commit strategy:**
```bash
# After completing a phase
git add .
git commit -m "feat: Complete Phase 3 - Domain Layer

- Added use cases (AddWater, GetHistory, Delete)
- Created DateUtils
- Configured Hilt injection

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"
```

**When to commit:**
- After completing a phase
- After fixing a critical bug
- At user's request
- Before major refactoring

---

## 7. Architecture & Code Quality

### 7.1 Clean Architecture Pattern

**Layer separation we used:**

```
UI Layer (Compose/ViewModels)
    ↓ uses
Domain Layer (Use Cases)
    ↓ uses
Data Layer (Repository → DAO/DataStore)
```

**Key principles:**
- Domain layer has NO Android dependencies
- UI depends on domain, NOT data
- Use interfaces in domain (WaterRepository)
- Implement in data layer (WaterRepositoryImpl)

### 7.2 Dependency Injection (Hilt)

**Pattern used:**
```kotlin
// ViewModel
@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCase: AddWaterUseCase,
    private val repository: WaterRepository
) : ViewModel()

// Use Case
class AddWaterUseCase @Inject constructor(
    private val repository: WaterRepository
)

// Repository
@Singleton
class WaterRepositoryImpl @Inject constructor(
    private val dao: WaterDao,
    private val settings: SettingsManager
) : WaterRepository
```

### 7.3 Reactive Programming (Flow)

**Pattern used:**
```kotlin
// Repository exposes Flow
fun getTodayTotal(): Flow<Int> = dao.getTotalForDate(today)

// ViewModel collects and updates UI state
viewModelScope.launch {
    getTodayTotalUseCase()
        .collect { total ->
            _uiState.update { it.copy(todayTotal = total) }
        }
}
```

### 7.4 Transaction Safety

**For multi-table operations:**
```kotlin
@Transaction
suspend fun addWaterIntakeTransaction(entry: WaterEntry) {
    insertEntry(entry)
    val total = getTotalForDateSync(entry.date)
    insertDailySummary(DailySummary(entry.date, total, ...))
}
```

---

## 8. Session Handoff Protocol

### 8.1 Before Ending Session

**Checklist:**
1. ✅ Update all phase checklists with [x]
2. ✅ Write session notes in DEVELOPMENT_PROGRESS.md
3. ✅ Update "NEXT STEPS" section
4. ✅ Mark current phase status (🟢🟡🔴)
5. ✅ Update `.claude/claude.md` if workflow changed
6. ✅ List any known issues/blockers
7. ✅ Git commit if appropriate

### 8.2 Starting New Session

**Checklist:**
1. ✅ Read `.claude/claude.md` thoroughly
2. ✅ Read `DEVELOPMENT_PROGRESS.md` thoroughly
3. ✅ Check git status (see what changed)
4. ✅ Find "NEXT STEPS" section
5. ✅ Ask user if they want to continue from there
6. ✅ Start TodoWrite for current tasks

### 8.3 Session Continuity Verification

**Self-check questions:**
- "Do I understand the project architecture?"
- "Do I know what phase I'm working on?"
- "Do I know what the previous agent completed?"
- "Do I have clear next steps?"
- "Are there any blockers I should know about?"

If answer is NO to any, read documentation again or ask user.

---

## 9. Templates

### 9.1 `.claude/claude.md` Template

```markdown
# Claude Code Agent Instructions - [Project Name]

**Last Updated:** [Date]
**Project:** [Project Name]
**Type:** [Android/Web/Backend/etc]

---

## 🎯 Project Overview

[Brief description of what this project does]

**Key Features:**
- Feature 1
- Feature 2
- Feature 3

---

## 📋 Workflow for Claude Agents

### When Starting a Session:

1. **Read this file first** (you're doing it now!)
2. **Open `DEVELOPMENT_PROGRESS.md`** - See current status and tasks
3. **Check "NEXT STEPS"** section - Know what to do next
4. **Use TodoWrite tool** - Track your current tasks
5. **Update progress as you go** - Keep progress file current

### When Ending a Session:

1. **Update `DEVELOPMENT_PROGRESS.md`**:
   - Mark completed tasks with [x]
   - Add session notes
   - Update "NEXT STEPS"
2. **Commit changes** (if appropriate)
3. **Verify** next agent can pick up where you left off

---

## 🏗️ Architecture

**Pattern:** [Clean Architecture / MVC / etc]

**Tech Stack:**
- Language: [Kotlin / TypeScript / etc]
- Framework: [Android / React / etc]
- DI: [Hilt / etc]
- Database: [Room / PostgreSQL / etc]

**Project Structure:**
```
src/
├── data/          # Data layer (Repository, DAO, APIs)
├── domain/        # Business logic (Use Cases)
├── ui/            # Presentation layer (ViewModels, UI)
├── di/            # Dependency injection modules
└── util/          # Utilities
```

---

## 📂 Important Files

**Documentation:**
- `DEVELOPMENT_PROGRESS.md` - **READ THIS EVERY SESSION**
- `PROJECT_PLAN.md` - Original requirements
- `APP_SUMMARY.md` - Final summary (created near end)

**Key Code Files:**
- [List important entry points]
- [List main configuration files]

---

## 🎨 Code Conventions

**Naming:**
- [Convention 1]
- [Convention 2]

**Comments:**
- [When to comment]

**Architecture:**
- [Key patterns to follow]

---

## 🧪 Testing Strategy

[How to test the app]

---

## ⚠️ Important Notes

- [Any critical information]
- [Things to avoid]
- [Known issues]

---

## 🤝 User Preferences

- [User's coding style preferences]
- [Communication preferences]
- [Any special requirements]

---

**Remember:** This is a multi-session project. Always update `DEVELOPMENT_PROGRESS.md` so the next Claude agent knows where you left off!
```

### 9.2 `DEVELOPMENT_PROGRESS.md` Template

```markdown
# [Project Name] - Development Progress Tracker

**Last Updated:** [Date]
**Current Phase:** Phase X - [Phase Name]
**Status:** [Ready to implement / In Progress / Blocked]

---

## Quick Status Overview

| Phase | Status | Progress |
|-------|--------|----------|
| Phase 1: [Name] | 🔴 Not Started | 0% |
| Phase 2: [Name] | 🔴 Not Started | 0% |
| Phase 3: [Name] | 🔴 Not Started | 0% |

**Legend:** 🔴 Not Started | 🟡 In Progress | 🟢 Completed

---

## NEXT STEPS (Start Here!)

### Current Focus: Phase X - [Phase Name]

**What to do next:**
1. [Clear step 1]
2. [Clear step 2]
3. [Clear step 3]

**Current Blockers:** [None / List blockers]

---

## Phase 1: [Phase Name]

**Status:** 🔴 Not Started
**Dependencies:** None
**Priority:** CRITICAL

### Tasks Checklist

#### 1.1 [Subtask Group]
- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

### Files Created/Modified in Phase 1
- [ ] `path/to/file.kt` - CREATED
- [ ] `path/to/file2.kt` - MODIFIED

---

## Phase 2: [Phase Name]

[Same structure as Phase 1]

---

## Session Notes

### Session 1 - [Date]
**Agent:** [Claude model]
**Work Done:**
- [What was completed]

**Key Features Implemented:**
- [Feature 1]
- [Feature 2]

**What Works Now:**
✅ [Working feature 1]
✅ [Working feature 2]

**Next Agent Should:**
1. [Clear instruction 1]
2. [Clear instruction 2]

---

## Known Issues / Blockers

[None currently / List issues]

---

## Important Notes for Future Agents

- [Note 1]
- [Note 2]
```

### 9.3 Session Notes Template

```markdown
### Session X - [Date]
**Agent:** [Claude model]
**Work Done:**
- ✅ **Phase X: [Status] - [Phase Name]**
  - [Specific file/feature created]
    - [Detail 1]
    - [Detail 2]
  - [Another file/feature]
    - [Detail 1]

**Key Features Implemented:**
- [Feature with brief explanation]
- [Another feature]

**What Works Now:**
✅ [Working feature 1]
✅ [Working feature 2]
✅ [Working feature 3]

**Next Agent Should:**
1. [Clear next step]
2. [Another next step]
3. [Final step]

**Technical Notes:**
- [Any important technical decisions]
- [Patterns used]
- [Things to be aware of]
```

---

## 10. Troubleshooting

### 10.1 Common Issues

**Issue:** New agent doesn't know what to do
**Solution:** Ensure `.claude/claude.md` has clear "When Starting a Session" section and `DEVELOPMENT_PROGRESS.md` has "NEXT STEPS"

**Issue:** Work gets duplicated
**Solution:** Mark tasks as [x] completed immediately in progress file

**Issue:** Files get lost between sessions
**Solution:** Commit work regularly, list all created files in session notes

**Issue:** Architecture confusion
**Solution:** Document architecture in `.claude/claude.md`, include examples

### 10.2 Session Recovery

**If a new agent is confused:**

1. Read `.claude/claude.md` completely
2. Read `DEVELOPMENT_PROGRESS.md` completely
3. Run `git log --oneline` to see recent work
4. Run `git status` to see current state
5. Ask user: "I see we're at Phase X. Should I continue from there?"

### 10.3 Progress Verification

**Periodically verify:**
```bash
# See what files exist
find src -type f -name "*.kt" | wc -l

# See git history
git log --oneline --graph

# Check current changes
git status --short
```

---

## 11. Success Metrics

**How to know this workflow is working:**

✅ New agents can start work in < 5 minutes
✅ No duplicate work across sessions
✅ No lost files or confusion
✅ User can see clear progress
✅ Testing happens at regular intervals
✅ Documentation stays up-to-date
✅ Code quality remains consistent

---

## 12. Lessons Learned

### What Worked Well (Water Logger Project)

1. **`.claude/claude.md`** - Single source of truth for agents
2. **Phase-based development** - Clear checkpoints, easy handoffs
3. **TodoWrite tool** - Kept agents focused on current task
4. **Session notes** - Excellent for tracking decisions and progress
5. **"NEXT STEPS" section** - Eliminated confusion about what to do next
6. **User touchpoints** - Regular testing prevented major issues
7. **Clean Architecture** - Made adding features easy and consistent

### What to Improve

1. **Testing automation** - Add unit tests as you build
2. **Earlier user testing** - Test after each phase, not just at end
3. **Code review checklist** - Add quality gates before phase completion

---

## 13. Quick Reference

### Essential Commands

```bash
# Start session
1. Open .claude/claude.md
2. Open DEVELOPMENT_PROGRESS.md
3. git status

# During work
- Use TodoWrite for tracking
- Update progress file as you go

# End session
1. Update DEVELOPMENT_PROGRESS.md
2. Write session notes
3. git commit (if appropriate)
```

### Essential Files

- `.claude/claude.md` - How to work on this project
- `DEVELOPMENT_PROGRESS.md` - What's done, what's next
- `PROJECT_PLAN.md` - Original requirements
- `APP_SUMMARY.md` - Final documentation (end of project)

### Key Tools

- **TodoWrite** - Task tracking within session
- **Read** - File exploration (do this before editing)
- **Edit/Write** - File modifications
- **Bash** - Git operations, testing

---

## 14. Conclusion

These practices enabled successful development of a complete Android app across multiple Claude agent sessions with:
- Zero lost work
- Seamless handoffs
- Consistent code quality
- Complete documentation
- High user satisfaction

**The key insight:** Treat multi-session development like a relay race. Each agent must:
1. Know where they are (read docs)
2. Know what to do (next steps)
3. Do the work (implement features)
4. Pass the baton (update docs)

Follow these practices and your multi-session projects will be just as successful!

---

**This document should be updated** as you learn new patterns and improve the workflow. It's a living document that captures institutional knowledge for Claude agents.

**Version History:**
- v1.0 (2025-10-27): Initial version based on Water Logger project

---

**Questions?** Refer back to the Water Logger project files as reference implementation of these practices.
