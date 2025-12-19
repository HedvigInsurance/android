# Claude Code Configuration

This directory contains configuration for Claude Code in this project.

## Notion MCP Server Setup

The project uses the Notion MCP server to allow Claude Code to interact with Notion workspaces.

### Prerequisites

- Node.js and npm installed
- Access to the Notion API key stored in 1Password

### Configuration

1. Retrieve the API key from 1Password:
   - Look for the entry named **"Notion MCP API Key"**
   - Copy the API key value

2. Create `.mcp.json` in the project root with the following configuration:

```json
{
  "mcpServers": {
    "notion": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@notionhq/notion-mcp-server"
      ],
      "env": {
        "OPENAPI_MCP_HEADERS": "{\"Authorization\": \"Bearer <YOUR_API_KEY>\", \"Notion-Version\": \"2022-06-28\"}"
      }
    }
  }
}
```

3. Replace `<YOUR_API_KEY>` with the API key from 1Password.

### Starting Claude Code with the MCP Server

Simply run Claude Code from the project directory:

```bash
claude
```

Claude Code automatically detects the `.mcp.json` file and starts the configured MCP servers. You should see the Notion tools become available once the server connects.

To verify the server is running, you can check available MCP tools by typing `/mcp` in Claude Code.

### Usage

When creating a PR Claude Code will look at your staged changes and try to find the corresponding ticket Notion, inserting the ticket ID in the PR title. 

### Troubleshooting

- **Server not connecting**: Ensure the API key is correctly formatted in the `OPENAPI_MCP_HEADERS` environment variable
- **Permission errors**: Verify that the Notion integration has access to the pages/databases you're trying to access in Notion's settings
- **First run slow**: The first run may take a moment as npx downloads the `@notionhq/notion-mcp-server` package
