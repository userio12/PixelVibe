# Termux Plan Mode Guide

OpenCode has a **Plan mode** that disables file changes and shows how it would implement a feature. Switch back to **Build mode** to make changes.

## Toggle

Since Termux has no Tab key, `agent_cycle` is remapped to:

```
Ctrl + T
```

Press **Ctrl+P** to cycle between Plan and Build modes. Watch the indicator in the lower-right corner of the TUI.

## Usage

1. Press **Ctrl+T** to enter Plan mode
2. Describe the feature you want
3. Review the plan
4. Press **Ctrl+T** to switch back to Build mode
5. Say "Go ahead and make the changes"
