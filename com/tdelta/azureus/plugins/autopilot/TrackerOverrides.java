/*
 * TrackerOverrides.java
 *
 * Created on June 14, 2005, 2:21 AM
 */

package com.tdelta.azureus.plugins.autopilot;

import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.biglybt.core.config.COConfigurationManager;
import com.biglybt.core.util.Constants;

/**
 * Manages tracker default overrides across the plugin and is also responsible for config table updates.
 *
 * @author clewis
 */
public class TrackerOverrides {
    private AutoPilot plugin;
    private Hashtable htOverrides;
    private Table     tConfig;
    
    public TrackerOverrides(AutoPilot plugin) {
        this.plugin = plugin;
        this.htOverrides = new Hashtable();
        this.Load();
    }
    
    public void Load() {
        try {
            this.htOverrides.clear();
        } catch(Exception ex) {
        }

        try {
            int count = COConfigurationManager.getIntParameter("AP_OVERRIDE_COUNT", 0);
            int i;
            if (count > 0) {
                for (i = 0; i < count; i++) {
                    try {
                        Hashtable newentry = new Hashtable();

                        String URL = COConfigurationManager.getStringParameter("AP_OVERRIDE_URL" + i);
                        newentry.put("trackerurl"     , URL);
                        newentry.put("trackermode"    , COConfigurationManager.getStringParameter("AP_OVERRIDE_MODE" + i));
                        newentry.put("trackerratio"   , COConfigurationManager.getStringParameter("AP_OVERRIDE_RATIO" + i));
                        newentry.put("trackerminseeds", COConfigurationManager.getStringParameter("AP_OVERRIDE_MINSEEDS" + i));
                        newentry.put("trackermaxseeds", COConfigurationManager.getStringParameter("AP_OVERRIDE_MAXSEEDS" + i));                   
                        newentry.put("trackerbitfield", COConfigurationManager.getStringParameter("AP_OVERRIDE_BITFIELD" + i));                   
                        htOverrides.put(URL.toLowerCase(), newentry);
                    } catch(Exception ex) {
                        plugin.writelog("exception while loading override #" + i + ex.getMessage());
                    }
                }
            }
        } catch(Exception ex) {
            plugin.writelog("exception while loading overrides: " + ex.getMessage());
        }
    }
    
    public void Save() {
        try {
            int i = 0;
            for (Enumeration e = htOverrides.keys(); e.hasMoreElements() ;) {
                try {
                    Hashtable htData = (Hashtable)htOverrides.get(e.nextElement());

                    COConfigurationManager.setParameter("AP_OVERRIDE_URL" + i, (String)htData.get("trackerurl"));
                    COConfigurationManager.setParameter("AP_OVERRIDE_MODE" + i, (String)htData.get("trackermode"));
                    COConfigurationManager.setParameter("AP_OVERRIDE_RATIO" + i, (String)htData.get("trackerratio"));
                    COConfigurationManager.setParameter("AP_OVERRIDE_MINSEEDS" + i, (String)htData.get("trackerminseeds"));
                    COConfigurationManager.setParameter("AP_OVERRIDE_MAXSEEDS" + i, (String)htData.get("trackermaxseeds"));
                    COConfigurationManager.setParameter("AP_OVERRIDE_BITFIELD" + i, (String)htData.get("trackerbitfield"));
                    i++;
                } catch(Exception ex) {
                    plugin.writelog("exception while saving override #" + i + ex.getMessage());
                }
            }
            COConfigurationManager.setParameter("AP_OVERRIDE_COUNT", i);
        } catch(Exception ex) {
            plugin.writelog("exception while saving overrides: " + ex.getMessage());
        }
    }
    
    public Hashtable Get(String URL) {
        try {
            return (Hashtable)htOverrides.get(URL.toLowerCase());
        } catch(Exception ex) {
            return null;
        }
    }
    
    public void Update(String URL, int Mode, float Ratio, int minSeed, int maxSeed, int Flags) {
        Update(URL, Mode, Ratio, minSeed, maxSeed, Flags, true, true);
    }
    public void Update(String URL, int Mode, float Ratio, int minSeed, int maxSeed, int Flags, boolean updatetable) {
        Update(URL, Mode, Ratio, minSeed, maxSeed, Flags, updatetable, true);
    }
    public void Update(String URL, int Mode, float Ratio, int minSeed, int maxSeed, int Flags, boolean updatetable, boolean savedata) {
        try {
            Hashtable newentry = new Hashtable();

            newentry.put("trackerurl"     , URL);
            newentry.put("trackermode"    , Integer.toString(Mode));
            newentry.put("trackerratio"   , Float.toString(Ratio));
            newentry.put("trackerminseeds", Integer.toString(minSeed));
            newentry.put("trackermaxseeds", Integer.toString(maxSeed));
            newentry.put("trackerbitfield", Integer.toString(Flags));

            htOverrides.put(URL.toLowerCase(), newentry);
            
            if (updatetable && (tConfig instanceof Table)) {
                int idx = FindTableKey(tConfig, URL);
                if (idx > -1) {
                    TableItem item = tConfig.getItem(idx);
                    item.setText(new String[] {URL, plugin.getratiotext(Mode, Ratio), minSeed > 0 ? Integer.toString(minSeed) : Constants.INFINITY_STRING, maxSeed > 0 ? Integer.toString(maxSeed) : Constants.INFINITY_STRING, GetFlagText(Flags)});
                }
            }
            
            if (savedata)
                Save();
        } catch(Exception ex) {
            plugin.writelog("exception while updating item: " + ex.getMessage());
        }
    }
    
    public void Add(String URL, int Mode, float Ratio, int minSeed, int maxSeed, int Flags) {
        Add(URL, Mode, Ratio, minSeed, maxSeed, Flags, true, true);
    }
    public void Add(String URL, int Mode, float Ratio, int minSeed, int maxSeed, int Flags, boolean updatetable) {
        Add(URL, Mode, Ratio, minSeed, maxSeed, Flags, updatetable, true);
    }
    public void Add(String URL, int Mode, float Ratio, int minSeed, int maxSeed, int Flags, boolean updatetable, boolean savedata) {
        try {
            if (htOverrides.containsKey(URL.toLowerCase())) {
                Update(URL, Mode, Ratio, minSeed, maxSeed, Flags, updatetable, savedata);
            } else {
                Hashtable newentry = new Hashtable();

                newentry.put("trackerurl"     , URL);
                newentry.put("trackermode"    , Integer.toString(Mode));
                newentry.put("trackerratio"   , Float.toString(Ratio));
                newentry.put("trackerminseeds", Integer.toString(minSeed));
                newentry.put("trackermaxseeds", Integer.toString(maxSeed));
                newentry.put("trackerbitfield", Integer.toString(Flags));

                htOverrides.put(URL.toLowerCase(), newentry);

                if (updatetable && (tConfig instanceof Table)) {

                    //Flags
                    TableItem newitem = new TableItem(tConfig, SWT.NONE);
                    newitem.setText(new String[] {URL, plugin.getratiotext(Mode, Ratio), minSeed > 0 ? Integer.toString(minSeed) : Constants.INFINITY_STRING, maxSeed > 0 ? Integer.toString(maxSeed) : Constants.INFINITY_STRING, GetFlagText(Flags)});
                    newitem.setData(URL.toLowerCase());
                }

                if (savedata)
                    Save();
            }
        } catch(Exception ex) {
            plugin.writelog("exception while adding item: " + ex.getMessage());
        }
    }
   
    public void Remove(String URL) {
        Remove(URL, true, true);
    }
    public void Remove(String URL, boolean updatetable) {
        Remove(URL, updatetable, true);
    }
    public void Remove(String URL, boolean updatetable, boolean savedata) {
        try {
            htOverrides.remove(URL);
            
            if (updatetable && (tConfig instanceof Table)) {
                int idx = FindTableKey(tConfig, URL);
                if (idx > -1) {
                    tConfig.remove(idx);
                }
            }
            
            if (savedata)
                Save();
        } catch(Exception ex) {
            plugin.writelog("exception while removing item: " + ex.getMessage());
        }
    }
    
    public void RegisterTable(Table table) {
        tConfig = table;
        if (tConfig instanceof Table)
            PopulateTable(tConfig);
    }
    
    public void PopulateRegisteredTable() {
        if (tConfig instanceof Table)
            PopulateTable(tConfig);
    }
    
    private int FindTableKey(Table table, String key) {
        String lckey = key.toLowerCase();
        for (int i = 0; i < table.getItemCount(); i++) {
            if (table.getItem(i).getData().toString().toLowerCase() == lckey)
                return i;
        }
        return -1;
    }
    
    private String GetFlagText(int Flags) {
        String sOutput;

        sOutput = "";
        if ((Flags & AutoPilot.APOPT_REMOVE_ON_STOP) > 0) {
            sOutput += "R";
        } else {
            sOutput += "-";
        }

        if ((Flags & AutoPilot.APOPT_DELETE_ON_STOP) > 0) {
            sOutput += "D";
        } else {
            sOutput += "-";
        }

        if ((Flags & AutoPilot.APOPT_MAX_SEED_INCLUSIVE) > 0) {
            sOutput += "I";
        } else {
            sOutput += "-";
        }
        
        return sOutput;
    }
    
    public void PopulateTable(Table table) {
        TableItem newitem;
        
        if (!(table instanceof Table))
            return;
        
        try {
            table.removeAll();

            int    itrackermode;
            float  fmaxratio;
            String finalratio;
                    
            for (Enumeration e = htOverrides.keys(); e.hasMoreElements() ;) {
                try {
                    Object key = e.nextElement();
                    Hashtable htData = (Hashtable)htOverrides.get(key);

                    newitem = new TableItem(table, SWT.NONE);

                    int minSeed = Integer.parseInt((String)htData.get("trackerminseeds"));
                    int maxSeed = Integer.parseInt((String)htData.get("trackermaxseeds"));

                    int FlagBF = 0;
                    try {
                        FlagBF = Integer.parseInt((String)htData.get("trackerbitfield"));
                    } catch(Exception ex) {
                    }

                    newitem.setText(new String[] {(String)htData.get("trackerurl"), plugin.getratiotext((String)htData.get("trackermode"), (String)htData.get("trackerratio")), minSeed > 0 ? Integer.toString(minSeed) : Constants.INFINITY_STRING, maxSeed > 0 ? Integer.toString(maxSeed) : Constants.INFINITY_STRING, GetFlagText(FlagBF)});
                    newitem.setData(key);
                } catch(Exception ex) {
                    plugin.writelog("exception while adding entry: " + ex.getMessage());
                }
            }
        } catch(Exception ex) {
            plugin.writelog("exception while populating table: " + ex.getMessage());
        }
    }
}
