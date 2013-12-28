# Break *Fast*

Current version: v1.0.1

Allows players to destroy blocks with one hit.

When enabled, you can break blocks in one hit. No special items required. You also have the option of having the block dropped when breaking it. Default behavior is not dropping broken blocks.


Commands
--------

#### /BreakFast
- alias: /bf
- description: enables/disables the Break Fast plugin for you.
- permissions: breakfast.use
- default: op

#### /BreakFastDrop
- alias: /bfd
- description: enables/disables dropping of blocks destroyed using Break Fast
- permissions: breakfast.use
- default: op

Usage
--------

Using Break *Fast* couldn't be easier, just enable Break *Fast* plugin then break blocks normally using left click.

Usage steps:
- Enable Break *Fast* with /BreakFast ( or /bf)
- Break blocks with left click. This will only take one hit now.
- [optionally] Enable Break *Fast* Drop with /BreakFastDrop (or /bfd)
- Enabling Break *Fast* Drop means when Break *Fast* is enabled, broken blocks will drop into the world normally. Great for strip mining

Configuration
--------

There is no configuration necessary for this plugin.

Permissions
--------

breakfast.use
- allows usage of /BreakFast and /BreakFastDrop commands
- defaults to op

Known Issues
--------

- Breaking mineral blocks such as coal/redstone/gold/iron will not drop the mineral even with Break *Fast* Drop enabled.
