# TriggeredBot

Telegram bot for user-defined triggered replies

## Commands

`/create_action`

This command requires additional text as well as an attachment

`/list_actions`

Does not require any additional parameters.

Triggers a reply from bot

`/delete_action`

Requires id of an action to delete.


## Example usage

You can use the following command to add a trigger with specifed action to the bot.
```
/create_action
TYPE: image
PATTERN: [Hh]ehe
RESOURCE: memes/
```
As an attachment add zip-file `memes.zip` with several images as its contents.

Now the message `hehe` will trigger a reply from the bot. It will select a random image from your archive and send it.

You can also specify caption for this image:
```
/create_action
TYPE: image
PATTERN: Much hehe
REPLY: Here's a meme!
RESOURCE: memes/
```

Or send only text, without an image:
```
/create_action
TYPE: image
PATTERN: [Hh]ehe?
REPLY: No meme today
```

With this command you will be able to get information about added actions, including their internal ID.
```
/list_actions
```

If you decide that you don't want the specific trigger anymore, you can delete it. `/delete_action` command takes an ID of the action as an argument.
```
/delete_action 1
```

## Build

You must source configuration from the enviromental variables.
```
source .env
```
Then you can package into a jar and run the bot as a jar-file:
```
mvn package && java -jar target/trigered-bot-${VERSION}.jar
```
or, alternatively:
```
mvn spring-boot:run
```

### Configuration

The configuration is done via enviromental variables. You must specify the bot token, semicolon-separated allowed chat identifiers, and database parameters. Note that only PostgreSQL is currently supported.

Here's an example of `.env` file for linux:
```
export tgAuthToken=YOUR-TELEGRAM-TOKEN
export tgChatId=111111111;222222222
export dbUrl=jdbc:postgresql://localhost:5432/triggeredbot
export dbUsername=your-db-username
export dbPassword=your-db-password
```

## TODO
- [ ] Resource size not calculated
- [ ] Resource size re-evaluation with new files detected
- [ ] /edit_action command
- [ ] Cron-actions
- [ ] Localization

