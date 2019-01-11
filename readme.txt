BiglyBT AutoPilot 0.6.1
https://github.com/BiglySoftware/BiglyBT-plugin-autopilot

WHAT IS AUTOPILOT
=================================

AutoPilot is a plugin module for the java BitTorrent client BiglyBT (https://biglybt.com).

AutoPilot manages seeding by allowing users to place individual ratio quotas on every torrent. Complimented by an advanced default & tracker override system, users may feel at rest knowing they are contributing back to their community exactly the amount desired.

FEATURES
=================================

 * Manages individual settings for each torrent.
 * Global Defaults. User-defined values newly added torrents inherit its settings from.
 * Tracker Overrides. User-defined values newly added torrents with a matching tracker inherit its settings from.
 * Ratio Quotas that define a torrent's maximum share ratio.
 * Minimum swarm seed conditional which forcefully suppresses termination when unfulfilled.
 * Maximum swarm seed conditional which ends seeding when met.
 * Optionally removes the torrent when seeding is complete.
 * Optionally deletes the torrent file upon removal.
 * Optionally terminates seeds when queuing incomplete torrents.
 * Optionally suppresses termination when seeds are force-started.
 * Optionally displays popup alerts when AutoPilot terminates a seed.
 * Custom control over the polling rate for optimal performance.

 See changelog.txt for version history and changes.

REQUIREMENTS:
=================================

BiglyBT 1.7.0.1 or higher
JRE     7       or higher

INSTALLATION
=================================

1) Choose Plugins->Installation Wizard from the main BiglyBT window.
2) Choose the 'By file' option and click the Next button.
3) Browse to the location where you have extracted the AutoPilot jar file and select it and Click the Next button.
4) Choose your desired Installation type and press Finish.
5) Restart BiglyBT.

## It is important that you restart BiglyBT after installation, as some UI components such as columns and context menu options will not be available, which will hinder proper use of the plugin.

## When upgrading, you must fully uninstall the previous version through BiglyBT' uninstall wizard.

CONFIGURATION OPTIONS
=================================

New Torrent Defaults:
------------------------------------
Each torrent is assigned its own unique pool of settings when added to the BiglyBT queue which will inherit from the global defaults you specify in the configuration panel. When installing AutoPilot for the first time, program defaults are supplied, so be sure to change these to your liking.

Ratio Stop Mode:
------------------------------------
There are three modes that you may assign to your torrent: "Unlimited Seeding", "Stop Immediately", and "User Defined". During unlimited seeding, all ratio-based processing for your torrent will be suppressed. When stop Immediately is chosen, the torrent is terminated as soon as it enters the seeding state, even if the torrent was already completed before starting. User defined will stop seeding when the ratio meets or exceeds the value entered in the Maximum share ratio text field.

Maximum Share Ratio:
------------------------------------
This field complements the Ratio Stop Mode, and is only used when the mode is set to 'User Defined'. Ratio entered must be in the BiglyBT ratio format of "x.xxx" where each whole number represents a complete copy.

Minimum swarm seed count:
------------------------------------
Seed termination will be suppressed when the seed count for the entire swarm falls below this number. Please note that this setting completely overrides all other settings including the maximum swarm seed conditional. A zero value disables this setting.

Maximum swarm seed count:
------------------------------------
Seeding will be terminated when the seed count for the entire swarm meets or exceeds this number. A zero value disables this setting. 

(NEW) Maximum swarm seed suppression:
------------------------------------
If "only when rato is met" option is checked next to the max seed conditional, processing for it will be supressed until the ratio is met or exceeded. Please note that when in the "Unlimited Seeding" mode the max seed conditional will NOT be supressed in this manner. Enabled by default.

(NEW) Remove when complete:
------------------------------------
When enabled, the torrent entry will automatically be removed from BiglyBT when seeding is completed. Disabled by default.

(NEW) and delete torrent file:
------------------------------------
When enabled, the torrent file will be deleted when seeding is completed. File is only deleted if the remove option is also enabled. Disabled by default.

Modifying Torrent Settings:
------------------------------------
After a torrent is added, its settings may only be altered with the modify dialog screen. To access the dialog, highlight the torrent within the My Torrents window and right-click on it to bring up the context menu. From this menu, select 'Change auto-stop settings for this torrent'.

(UPDATED) Tracker Overrides:
------------------------------------
Overrides allow you to specify an alternative set of defaults when the tracker of your newly added torrent matches an existing one in the list. 

To add a tracker to your list, simply right click on the torrent in the My Torrents window and choose "Add tracker to override list". Newly added trackers will automatically inherit the global defaults, which you may then modify to your specifications.

To modify an existing entry, open AutoPilot's configuration panel and choose the tracker from the override list and click the modify button. Highlighting multiple trackers will open multiple modify dialogs simultaneously.

To remove an existing entry, simply highlight the tracker(s) in the list and click the remove button.

Some torrent options have been consolodated into the Flags column for easier display. When a particular option is enabled, a single letter representing it will be displayed. When disabled, a hyphen ("-") will appear in its place instead.

Stop on Queued Incomplete Torrents:
------------------------------------
All seeding is terminated when incomplete torrents are queued. This feature is designed for BiglyBT' "Max active torrents" setting, which when activated can actually impede any downloading if the amount of active seeds clogs every "active" slot. When this setting is enabled, your slots are automatically freed up to make way for new content.

Please note that this is a global setting that affects all torrents managed by BiglyBT. Currently, there are no 'smart' measures in place to ensure that seeding is only stopped when necessary, the amount of slots stopped, and also does not take seeding priority into account. This option is turned off by default and exists through legacy code. A future release may update this behavior to be more cooperative, but for now some may still find it useful.

Prevent stop when force-started:
------------------------------------
When enabled, AutoPilot processing is suppressed on seeding torrents that have been force-started. This option is enabled by default.

Show alert when taking action:
------------------------------------
When enabled, a popup notification is displayed when AutoPilot terminates a seeding torrent. Useful for tripping a screen-saver on a secondary PC or just giving you a reminder when your torrent is complete. Enabled by default.

Polling Interval:
------------------------------------
This allows you to fine-tune AutoPilot by setting the interval at which the state of your torrents are polled. Enter the value in milliseconds (1000/second), 1 second being the default. Only torrents that are actually in the seeding state are; each interval rotates through the entire list once before waiting for the next frame. This can be useful on slower machines, especially if you are seeding a lot of torrents.




~Enjoy

Carl Lewis
3D Delta Developers
http://www.3ddelta.com/
