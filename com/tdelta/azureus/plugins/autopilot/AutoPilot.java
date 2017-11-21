/*
 *  AutoPilot
 *  Created by Carl Lewis on April 24, 2005, 6:05 PM
 *
 *  Copyright (C) 2005 3D Delta Developers, All Rights Reserved.
 *  http://www.3ddelta.com/
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * */

package com.tdelta.azureus.plugins.autopilot;

import com.biglybt.pif.Plugin;
import com.biglybt.pif.PluginConfig;
import com.biglybt.pif.PluginException;
import com.biglybt.pif.PluginInterface;
import com.tdelta.azureus.plugins.autopilot.UI.*;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import com.biglybt.pif.ui.model.*;
import com.biglybt.pif.utils.*;
import com.biglybt.pif.logging.*;
import com.biglybt.pif.download.*;
import com.biglybt.pif.config.*;
import com.biglybt.pif.torrent.TorrentAttribute;
import com.biglybt.pif.ui.menus.MenuItem;
import com.biglybt.pif.ui.menus.MenuItemListener;
import com.biglybt.pif.ui.tables.TableCell;
import com.biglybt.pif.ui.tables.TableCellRefreshListener;
import com.biglybt.pif.ui.tables.TableColumn;
import com.biglybt.pif.ui.tables.TableContextMenuItem;
import com.biglybt.pif.ui.tables.TableManager;
import com.biglybt.pif.ui.tables.TableRow;
import com.biglybt.core.util.Constants;

/**
 * AutoPilot Azureus Plugin. Monitors download activity and automatically stops based on user preferences
 *
 * @author clewis
 */
public class AutoPilot implements Plugin, DownloadManagerListener, DownloadListener, TableCellRefreshListener {
    //Public Variables
    public static final boolean   DEBUG                             = false;
    public static final int       CONFIG_DEFAULT_STOPMODE           = 1;
    public static final String    CONFIG_DEFAULT_MAXRATIO           = "1.0";
    public static final boolean   CONFIG_DEFAULT_STOPONQUEUE        = false;
    public static final int       CONFIG_DEFAULT_MAXSEEDTHRESH      = 50;
    public static final int       CONFIG_DEFAULT_MINSEEDTHRESH      = 10;
    public static final boolean   CONFIG_DEFAULT_REMOVEONSTOP       = false;
    public static final boolean   CONFIG_DEFAULT_DELETEONSTOP       = false;
    public static final boolean   CONFIG_DEFAULT_MAXSEEDINCLUSIVE   = true;

    public static final boolean   CONFIG_DEFAULT_SKIPFORCESTART     = true;
    public static final int       CONFIG_DEFAULT_UPDATEFREQUENCY    = 1000;
    public static final boolean   CONFIG_DEFAULT_ALERTONSTOP        = true;
    
    public static final int       AP_UNLIMITED                      = -1;
    public static final int       AP_AUTOSTOP                       = 0;
    public static final int       AP_USERRATIO                      = 1;

    public static final int       APOPT_REMOVE_ON_STOP              = 1;
    public static final int       APOPT_DELETE_ON_STOP              = 2;
    public static final int       APOPT_MAX_SEED_INCLUSIVE          = 4;

    public TorrentAttribute       attributeAutoStopMode;
    public TorrentAttribute       attributeMaxShareRatio;
    public TorrentAttribute       attributeMaxSeedThreshold;
    public TorrentAttribute       attributeMinSeedThreshold;
    public TorrentAttribute       attributeOptionsBF;
    
    //Private Variables
    private static final String   COLUMN_ID_RATIO                   = "RatioColumn";

    private String                MODE_AUTOSTOP;

    private PluginInterface       pluginInterface;
    private LoggerChannel         pluginLog;
    private BasicPluginViewModel  pluginModelBasic;
    private LocaleUtilities       pluginLocale;
    private PluginConfig config;

    private int                   queuedcache;
    private SeedTracker           seedtracker;
    private TrackerOverrides      trackeroverrides;
    private TableColumn           ratioColumn;
    private TableColumn           myd_ratioColumn;
    
    
    //================================================================================================
    // Initializer
    //================================================================================================           
    @Override
    public void initialize(PluginInterface pluginInterface) throws PluginException {
        try {
            //Grab the various interfaces used throughout the plugin
            this.pluginInterface = pluginInterface;
            pluginLog = pluginInterface.getLogger().getChannel("AutoPilot");
            pluginLocale = pluginInterface.getUtilities().getLocaleUtilities();
            queuedcache = 0;
            
            //Create a window for console-like logging (debug mode only)
            if (DEBUG) {
                pluginModelBasic = pluginInterface.getUIManager().createBasicPluginViewModel("AutoPilot");
                pluginModelBasic.getActivity().setVisible(false);
                pluginModelBasic.getProgress().setVisible(false);
            }

            //Global localized text
            MODE_AUTOSTOP = getLocalisedMessageText("autopilot.mode.autostop");

            //Create the override manager and load the settings from the previous session
            trackeroverrides = new TrackerOverrides(this);

            //Create and add the SWT Configuration Panel
            AutoPilotConfig apconfig = new AutoPilotConfig(this, trackeroverrides);
            pluginInterface.addConfigSection(apconfig);
            config = pluginInterface.getPluginconfig();
            
            //Register the download attributes with the torrent manager
            attributeMaxShareRatio    = pluginInterface.getTorrentManager().getPluginAttribute("maxshareratio");
            attributeAutoStopMode     = pluginInterface.getTorrentManager().getPluginAttribute("autostopmode");
            attributeMaxSeedThreshold = pluginInterface.getTorrentManager().getPluginAttribute("maxseedthreshold");
            attributeMinSeedThreshold = pluginInterface.getTorrentManager().getPluginAttribute("minseedthreshold");
            attributeOptionsBF        = pluginInterface.getTorrentManager().getPluginAttribute("ap_bitfield");
            
            //Create the context menu and the ratio column
            addMyTorrentsMenu();
            addMyTorrentsColumn();

            //Initialize the seed tracker
            seedtracker = new SeedTracker();

            //Display initialized text in console
            if (DEBUG) {
                if (this.pluginModelBasic != null)
                    this.pluginModelBasic.getLogArea().appendText(MessageFormat.format(getLocalisedMessageText(DEBUG?"autopilot.message.debuginitialized":"autopilot.message.initialized"), new java.lang.Object[] {pluginInterface.getPluginVersion()}));
            }

            //Capture events from the download manager
            pluginInterface.getDownloadManager().addListener(this);
        } catch(Exception ex) {
            String msg = MessageFormat.format(getLocalisedMessageText("autopilot.error.init"), new java.lang.Object[] {ex.getLocalizedMessage()});
            alert(LoggerChannel.LT_ERROR, msg);
            writelog(MODE_AUTOSTOP + " " + msg);
        }
    }

    private int ConstructDefaultBitfield() {
        int field = 0;
        if (config.getUnsafeBooleanParameter("autopilot_as_removeonstop", CONFIG_DEFAULT_REMOVEONSTOP))
            field = field | AutoPilot.APOPT_REMOVE_ON_STOP;
        if (config.getUnsafeBooleanParameter("autopilot_as_deleteonstop", CONFIG_DEFAULT_DELETEONSTOP))
            field = field | AutoPilot.APOPT_DELETE_ON_STOP;
        if (config.getUnsafeBooleanParameter("autopilot_as_defaultmaxseedinclusive", CONFIG_DEFAULT_MAXSEEDINCLUSIVE))
            field = field | AutoPilot.APOPT_MAX_SEED_INCLUSIVE;
        return field;
    }

    //================================================================================================
    // EVENTS
    //================================================================================================
    @Override
    public void downloadAdded(Download download) {
        if (DEBUG) writelog("DevMsg: Download added:" + download.getName());

        Hashtable htDefaults = trackeroverrides.Get(download.getTorrent().getAnnounceURL().toString());
        if (htDefaults != null) {
            // The tracker for this download is in the override list. Set download to these defaults instead of the global defaults.
            // Also, since these downloads can be "added" when azureus first starts up, only merge them in if they dont already exist (no clobbering).
            if (download.getAttribute(attributeAutoStopMode) == null)
                download.setAttribute(attributeAutoStopMode, (String)htDefaults.get("trackermode"));
            if (download.getAttribute(attributeMaxShareRatio) == null)
                download.setAttribute(attributeMaxShareRatio, (String)htDefaults.get("trackerratio"));
            if (download.getAttribute(attributeMinSeedThreshold) == null)
                download.setAttribute(attributeMinSeedThreshold, (String)htDefaults.get("trackerminseeds"));
            if (download.getAttribute(attributeMaxSeedThreshold) == null)
                download.setAttribute(attributeMaxSeedThreshold, (String)htDefaults.get("trackermaxseeds"));
            if (download.getAttribute(attributeOptionsBF) == null)
                download.setAttribute(attributeOptionsBF, (String)htDefaults.get("trackerbitfield"));
        } else {
            // Set download to the global defaults.
            // Also, since these downloads can be "added" when azureus first starts up, only merge them in if they dont already exist (no clobbering).
            if (download.getAttribute(attributeAutoStopMode) == null)
                download.setAttribute(attributeAutoStopMode, "" + config.getUnsafeIntParameter("autopilot_as_defaultmaxratiotype", CONFIG_DEFAULT_STOPMODE));
            if (download.getAttribute(attributeMaxShareRatio) == null)
                download.setAttribute(attributeMaxShareRatio, config.getUnsafeStringParameter("autopilot_as_defaultmaxratio", CONFIG_DEFAULT_MAXRATIO));
            if (download.getAttribute(attributeMinSeedThreshold) == null)
                download.setAttribute(attributeMinSeedThreshold, "" + config.getUnsafeIntParameter("autopilot_as_defaultminseedthreshold", CONFIG_DEFAULT_MINSEEDTHRESH));
            if (download.getAttribute(attributeMaxSeedThreshold) == null)
                download.setAttribute(attributeMaxSeedThreshold, "" + config.getUnsafeIntParameter("autopilot_as_defaultmaxseedthreshold", CONFIG_DEFAULT_MAXSEEDTHRESH));
            if (download.getAttribute(attributeOptionsBF) == null)
                download.setAttribute(attributeOptionsBF, "" + ConstructDefaultBitfield());
        }

        //Add the event and update the queued count cache
        download.addListener(this);
        queuedcache = GetQueuedCount();

        //If a torrent is already in the seeding state when autopilot initializes (just installed or Azureus just started up), then call the event handler
        if (download.getState() == Download.ST_SEEDING)
            this.stateChanged(download, Download.ST_DOWNLOADING, Download.ST_SEEDING);
    }

    @Override
    public void downloadRemoved(Download download) {
        if (DEBUG) writelog("DevMsg: Download removed:" + download.getName());

        //Release resources and update queued count cache
        seedtracker.removeseeder(download);
        download.removeListener(this);
        queuedcache = GetQueuedCount();
    }

    @Override
    public void stateChanged(Download download, int old_state, int new_state) {
        if (DEBUG) writelog("DevMsg: Download state changed:" + download.getName() + ": " + Download.ST_NAMES[old_state] + " -> " + Download.ST_NAMES[new_state]);

        //Update queued count cache
        queuedcache = GetQueuedCount();

        //Torrent has transitioned to the seeding state. Start monitoring this torrent.
        if (new_state == Download.ST_SEEDING) {
            seedtracker.addseeder(download);
        }
    }

    @Override
    public void positionChanged(Download download, int oldPosition, int newPosition) {
    }

    @Override
    public void refresh(TableCell cell) {
        try {
            Object dataSource = cell.getDataSource();
            if (dataSource == null || !(dataSource instanceof Download)) {
                return;
            }

            Download download  = (Download)dataSource;
            cell.setText(getratiotext(download.getAttribute(attributeAutoStopMode), download.getAttribute(attributeMaxShareRatio)));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    //================================================================================================
    // Utility Functions
    //================================================================================================
    public PluginInterface getInterface() {
        return this.pluginInterface;
    }

    public static int SafeIntParse(String Value) {
        try {
            return Integer.parseInt(Value);
        } catch(Exception ex) {
            return 0;
        }
    }

    public static float SafeFloatParse(String Value) {
        try {
            return Float.parseFloat(Value);
        } catch(Exception ex) {
            return 0;
        }
    }

    public void UpdateRatioColumn() {
        this.ratioColumn.invalidateCells();
        this.myd_ratioColumn.invalidateCells();
    }

    private void addMyTorrentsColumn() {
        TableManager tableManager = pluginInterface.getUIManager().getTableManager();

        ratioColumn = tableManager.createColumn(TableManager.TABLE_MYTORRENTS_COMPLETE, COLUMN_ID_RATIO);
        ratioColumn.setAlignment(TableColumn.ALIGN_TRAIL);
        ratioColumn.setPosition(10);
        ratioColumn.setRefreshInterval(TableColumn.INTERVAL_INVALID_ONLY);
        ratioColumn.setType(TableColumn.TYPE_TEXT);
        ratioColumn.addCellRefreshListener(this);
        tableManager.addColumn(ratioColumn);

        myd_ratioColumn = tableManager.createColumn(TableManager.TABLE_MYTORRENTS_INCOMPLETE, COLUMN_ID_RATIO);
        myd_ratioColumn.setAlignment(TableColumn.ALIGN_TRAIL);
        myd_ratioColumn.setPosition(10);
        myd_ratioColumn.setRefreshInterval(TableColumn.INTERVAL_INVALID_ONLY);
        myd_ratioColumn.setType(TableColumn.TYPE_TEXT);
        myd_ratioColumn.addCellRefreshListener(this);
        tableManager.addColumn(myd_ratioColumn);
    }

    private void addMyTorrentsMenu()  {
        //----------------------------------------------------------------------------------------------------------------------------------
        // Modify torrent settings
        //----------------------------------------------------------------------------------------------------------------------------------
        MenuItemListener listener = new MenuItemListener() {
            @Override
            public void selected(MenuItem _menu, Object _target) {
                Download download = (Download)((TableRow)_target).getDataSource();
                if (download == null || download.getTorrent() == null){
                    return;
                }
                new AutoPilotRatioDialog(AutoPilot.this, download);
            }
        };

        TableContextMenuItem menu1 = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE, "autopilot.contextmenu.changemaxratio");
        TableContextMenuItem menu2 = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_COMPLETE,   "autopilot.contextmenu.changemaxratio");
        menu1.addListener(listener);
        menu2.addListener(listener);

        //----------------------------------------------------------------------------------------------------------------------------------
        // Add new torrent override
        //----------------------------------------------------------------------------------------------------------------------------------
        MenuItemListener listener2 = new MenuItemListener() {
            @Override
            public void selected(MenuItem _menu, Object _target) {
                Download download = (Download)((TableRow)_target).getDataSource();
                if (download == null || download.getTorrent() == null){
                    return;
                }

                trackeroverrides.Add(download.getTorrent().getAnnounceURL().toString(), config.getUnsafeIntParameter("autopilot_as_defaultmaxratiotype", CONFIG_DEFAULT_STOPMODE), SafeFloatParse(config.getUnsafeStringParameter("autopilot_as_defaultmaxratio", CONFIG_DEFAULT_MAXRATIO)), config.getUnsafeIntParameter("autopilot_as_defaultminseedthreshold", CONFIG_DEFAULT_MINSEEDTHRESH), config.getUnsafeIntParameter("autopilot_as_defaultmaxseedthreshold", CONFIG_DEFAULT_MAXSEEDTHRESH), ConstructDefaultBitfield());
            }
        };

        TableContextMenuItem menu3 = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_INCOMPLETE, "autopilot.contextmenu.addtracker");
        TableContextMenuItem menu4 = pluginInterface.getUIManager().getTableManager().addContextMenuItem(TableManager.TABLE_MYTORRENTS_COMPLETE,   "autopilot.contextmenu.addtracker");
        menu3.addListener(listener2);
        menu4.addListener(listener2);
    }

    public int GetQueuedCount() {
        int i, cnt;

        Download[] downloads = pluginInterface.getDownloadManager().getDownloads(false);
        cnt = 0;
        for (i = 0; i < downloads.length; i++) {
            if (downloads[i].getState() == Download.ST_QUEUED && !downloads[i].isComplete())
                cnt++;
        }
        return cnt;
    }

    public String getLocalisedMessageText(String key) {
        return pluginLocale.getLocalisedMessageText(key);
    }

    public void alert(int type, String text) {
        if (this.pluginLog != null)
            this.pluginLog.logAlert(type, text);
    }

    public void writelog(String text) {
        if (this.pluginLog != null)
            this.pluginLog.log(text);
        if (this.pluginModelBasic != null)
            this.pluginModelBasic.getLogArea().appendText("\n" + text);
    }

    public String getratiotext(String Mode, String Ratio) {
        int   iMode;
        float fRatio;

        try {
            iMode = SafeIntParse(Mode);
            fRatio = SafeFloatParse(Ratio);
        } catch(Exception ex) {
            iMode = -1;
            fRatio = 0;
        }
        return getratiotext(iMode, fRatio);
    }
    public String getratiotext(int Mode, float ratio) {
        String finalratio;
        switch(Mode) {
            case AutoPilot.AP_UNLIMITED:
                //Do not auto-stop this torrent when in the seeding state
                finalratio = Constants.INFINITY_STRING;
                break;
            case AutoPilot.AP_AUTOSTOP:
                //Automatically stop this torrent when in the seeding state
                finalratio = getLocalisedMessageText("autopilot.column.ratiotype.stopimmediatly");
                break;
            case AutoPilot.AP_USERRATIO:
                //User specified ratio. Format and display user ratio in the cell
                NumberFormat maxratioformatted = NumberFormat.getInstance();
                maxratioformatted.setMaximumFractionDigits(3);
                maxratioformatted.setMinimumFractionDigits(3);
                finalratio = maxratioformatted.format(ratio);
                break;
            default:
                finalratio = getLocalisedMessageText("autopilot.column.ratiotype.unknown");
                break;
        }
        return finalratio;
    }

    //================================================================================================
    // Auto-Stop Rule Tracking Class
    //================================================================================================
    private class SeedTracker implements UTTimerEventPerformer, ConfigParameterListener {
        private List            seedList;
        private UTTimer         timer;
        private int             currentfrequency;
        private ConfigParameter updatefrequency;

        public SeedTracker() {
            this.seedList = new ArrayList();
            updatefrequency = config.getParameter("autopilot_as_interval");
            updatefrequency.addConfigParameterListener(this);
        }

        @Override
        public void configParameterChanged(ConfigParameter param) {
            try {
                if (param.equals(updatefrequency)) {
                    // If the property is a new value, then destroy the old timer and create a new one
                    if (currentfrequency != config.getUnsafeIntParameter("autopilot_as_interval", CONFIG_DEFAULT_UPDATEFREQUENCY)) {
                        currentfrequency = config.getUnsafeIntParameter("autopilot_as_interval", CONFIG_DEFAULT_UPDATEFREQUENCY);
                        timer.destroy();
                        timer = pluginInterface.getUtilities().createTimer("AP.AS.SEEDTRACKER");
                        timer.addPeriodicEvent(currentfrequency, this);
                    }
                }
            } catch(Exception ex) {
                String msg = MessageFormat.format(getLocalisedMessageText("autopilot.error.freqchange"), new java.lang.Object[] {ex.getLocalizedMessage()});
                alert(LoggerChannel.LT_ERROR, msg);
                writelog(MODE_AUTOSTOP + " " + msg);
            }
        }

        public void addseeder(Download download) {
            if (download.getState() != Download.ST_SEEDING)
                return;
            if (seedList.isEmpty()) {
                // Start the timer event used to check the download if it hasnt already been
                if (timer == null) {
                    currentfrequency = config.getUnsafeIntParameter("autopilot_as_interval", CONFIG_DEFAULT_UPDATEFREQUENCY);
                    timer = pluginInterface.getUtilities().createTimer("AP.AS.SEEDTRACKER");
                    timer.addPeriodicEvent(currentfrequency, this);
                }
            }
            seedList.add(download);
        }

        public void removeseeder(Download download) {
            if (download.getState() == Download.ST_SEEDING)
                return;
            if (!seedList.contains(download))
                return;
            seedList.remove(download);
            if (seedList.isEmpty()) {
                timer.destroy();
                timer = null;
            }
        }

        private void StopDownload(Download download, String msg) {
            try {
                //Stop the download
                download.stop();

                //Retrieve bitfield and remove/delete torrent if required
                int config_flags = SafeIntParse(download.getAttribute(attributeOptionsBF));
                if ((config_flags & APOPT_REMOVE_ON_STOP) > 0) {
                    download.remove((config_flags & APOPT_DELETE_ON_STOP) > 0, false);
                }

                //Display alert if required
                if (config.getUnsafeBooleanParameter("autopilot_as_alertonstop", CONFIG_DEFAULT_ALERTONSTOP))
                    alert(LoggerChannel.LT_INFORMATION, msg);
                writelog(MODE_AUTOSTOP + " " + msg);
            } catch(DownloadException ex) {
                String msg2 = MessageFormat.format(getLocalisedMessageText("autopilot.error.seedstop"), new java.lang.Object[] {ex.getLocalizedMessage()});
                alert(LoggerChannel.LT_ERROR, msg2);
                writelog(MODE_AUTOSTOP + " " + msg2);
            } catch(Exception ex) {
                writelog("Exception thrown while stopping download: " + ex.getMessage());
            }
        }

        @Override
        public void perform(UTTimerEvent event) {
            try {
                ListIterator liDownload = seedList.listIterator();
                while (liDownload.hasNext()) {
                    Download download = (Download)liDownload.next();

                    if (download == null)
                        continue;

                    if (download.getState() == Download.ST_SEEDING) {
                        // The torrent is currently locked, so ignore for now
                        if (download.isStartStopLocked())
                            continue;

                        // If user had chosen to ignore ForceStart torrents, then simply return here
                        if (download.isForceStart() && config.getUnsafeBooleanParameter("autopilot_as_skipforcestarts", CONFIG_DEFAULT_SKIPFORCESTART))
                            continue;

                        // Stop processing if the swarm seed count has fallen below the minimum seed threshold
                        int seedcount = download.getLastScrapeResult().getSeedCount();
                        int config_minseedthreshold = SafeIntParse(download.getAttribute(attributeMinSeedThreshold));
                        if (config_minseedthreshold > 0 && seedcount < config_minseedthreshold)
                            continue;

                        // Stop on Queue
                        if (config.getUnsafeBooleanParameter("autopilot_as_stoponqueue", CONFIG_DEFAULT_STOPONQUEUE) && (queuedcache > 0)) {
                            String msg = MessageFormat.format(getLocalisedMessageText("autopilot.message.queuedstop"), new java.lang.Object[] {download.getName()});
                            StopDownload(download, msg);
                            continue;
                        }
                               
                        // Stop on Ratio
                        int config_stopmode         = SafeIntParse(download.getAttribute(attributeAutoStopMode));
                        int config_flags            = SafeIntParse(download.getAttribute(attributeOptionsBF));
                        int config_maxseedthreshold = SafeIntParse(download.getAttribute(attributeMaxSeedThreshold));

                        switch (config_stopmode) {
                            case AutoPilot.AP_AUTOSTOP:
                                StopDownload(download, MessageFormat.format(getLocalisedMessageText("autopilot.message.complete"), new java.lang.Object[] {download.getName()}));
                                break;
                            case AutoPilot.AP_UNLIMITED:
                                // Stop on max seed threshold regardless of exclusivity
                                if (config_maxseedthreshold > 0 && (config_maxseedthreshold < seedcount))
                                    StopDownload(download, MessageFormat.format(getLocalisedMessageText("autopilot.message.maxseedstop"), new java.lang.Object[] {download.getName(), Integer.toString(config_maxseedthreshold)}));
                                break;
                            case AutoPilot.AP_USERRATIO:
                                // Suppress max seed processing if in INCLUSIVE mode. Inclusive mode ensures that the ratio is met before terminating
                                if (((config_flags & APOPT_MAX_SEED_INCLUSIVE) < 1) && config_maxseedthreshold > 0 && (config_maxseedthreshold < seedcount)) {
                                    StopDownload(download, MessageFormat.format(getLocalisedMessageText("autopilot.message.maxseedstop"), new java.lang.Object[] {download.getName(), Integer.toString(config_maxseedthreshold)}));
                                    break;
                                }
                                
                                // Process ratio
                                int   rawratio = download.getStats().getShareRatio();
                                float fmaxratio = SafeFloatParse(download.getAttribute(attributeMaxShareRatio));
                                int   maxratio = (int)(fmaxratio * 1000);
                                
                                if (DEBUG) writelog("DevMsg: Polling '" + download.getName() + "': Share Ratio is: " + rawratio + " - TARGET: " + maxratio);

                                if (rawratio >= maxratio) {
                                    //Format ratio for display in alert
                                    NumberFormat maxratioformatted = NumberFormat.getInstance();
                                    maxratioformatted.setMaximumFractionDigits(3);
                                    maxratioformatted.setMinimumFractionDigits(3);
                                    StopDownload(download, MessageFormat.format(getLocalisedMessageText("autopilot.message.ratioreached"), new java.lang.Object[] {download.getName(), maxratioformatted.format(fmaxratio)}));
                                }
                                break;
                        }
                    } else {
                        //Torrent is no longer seeding, so cancel the timer event and release references
                        if (DEBUG) writelog("DevMsg: '" + download.getName() + "' is no longer seeding.");
                        this.removeseeder(download);
                    }
                }
            } catch(Exception ex) {
                writelog("Exception thrown while processing download: " + ex.getMessage());
            }
        }
    }
}
