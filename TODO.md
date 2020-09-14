
# TODO:

## Database

- [ ] Update to the latest database changes.
  - [ ] Update the defined structures.
  - [ ] Fix the synchronization tasks.
- [ ] Define the structure for ongoing Telegram announcements.
- [ ] Update the ongoing announcements in the database every group of chats,
not for each chat (that's too spammy and consumes the db bandwidth).

## Settings Menu

- [ ] Implement the minimum price option.
- [ ] Add a close menu button.
- [ ] Create a sub-menu for filtering by store.
  - [ ] Add buttons to enable or disable all.
- [ ] Create `/channel_menu` for configuring the bot on a channel.

## Miscellaneous

- [ ] Improve the `/free` command with inline buttons.
- [ ] Documenting the bot for users:
  - [ ] Write the about text of the bot.
  - [ ] Write the description of the bot.
  - [ ] Write the `/help` command of the bot.
  - [ ] Write the help option in the configuration menu.
  - [ ] Write a better welcoming message.
  - [ ] Write a welcoming message for groups.
- [ ] Implement the `support bot` button.

## Maintenance

- [ ] Use SLF4J framework for logging.
- [ ] Create a files logging system if there's no one.
  - [ ] Switch to a new log file at 00:00 each day.
  - [ ] Compress the old log files.
  - [ ] Archive each month in a `.zip`.
- [ ] Use Prometheus for collecting metrics.
  - [ ] Setup the scraper.
  - [ ] Setup the Grafana dashboard.
  - [ ] Setup Grafana alerts.
