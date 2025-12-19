#!/bin/bash
# Hook: Notify Claude that a PR is being created
# This hook triggers before gh pr create to remind Claude to look up the Notion ticket

# Read the hook input from stdin
INPUT=$(cat)

# Log for audit trail (DORA compliance)
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
LOG_DIR="${HOME}/.claude/audit-logs"
mkdir -p "$LOG_DIR"
echo "{\"timestamp\":\"$TIMESTAMP\",\"event\":\"pr_creation_hook\",\"input\":$INPUT}" >> "$LOG_DIR/notion-mcp-audit.jsonl"

# Output a reminder to Claude (will be shown in the conversation)
cat <<EOF
{
  "status": "continue",
  "message": "PR creation detected. Remember to search the Notion R&D board for the relevant ticket and include the ticket ID in the PR title."
}
EOF