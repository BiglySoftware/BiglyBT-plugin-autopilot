package com.tdelta.azureus.plugins.autopilot.UI;

import com.tdelta.azureus.plugins.autopilot.APFloatParameter;
import com.tdelta.azureus.plugins.autopilot.AutoPilot;
import com.tdelta.azureus.plugins.autopilot.TrackerOverrides;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.plaf.FontUIResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.biglybt.ui.swt.config.BooleanParameter;
import com.biglybt.pif.ui.config.ConfigSection;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.config.*;
import com.biglybt.ui.swt.pif.UISWTConfigSection;

/**
 * AutoPilot's SWT UI Configuration Panel
 *
 * @author clewis
 */
public class AutoPilotConfig implements UISWTConfigSection {
    private AutoPilot        plugin;
    private Table            lvTrackerDefaults;
    private List             listOverrides;
    private TrackerOverrides overrides;
    private BooleanParameter chkros;
    private BooleanParameter chkdos;
    
    public AutoPilotConfig(AutoPilot plugin, TrackerOverrides overrides) {
        this.plugin = plugin;
        this.listOverrides = new ArrayList();
        this.overrides = overrides;
    }
    
	@Override
	public int maxUserMode() {
		return 0;
	}
	
    @Override
    public String configSectionGetParentSection() {
        return ConfigSection.SECTION_PLUGINS;
    }
    
    @Override
    public String configSectionGetName() {
        return "autopilot.name";
    }
    
    @Override
    public Composite configSectionCreate(Composite parent) {
        GridData gridData;
        GridLayout layout;
        Label label;
        Composite configcontainer;

        try {
            configcontainer = new Composite(parent, SWT.NULL);       
            gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            configcontainer.setLayoutData(gridData);
            layout = new GridLayout();
            layout.marginHeight = 0;
            configcontainer.setLayout(layout);
            {
                Group gAutoStop = new Group(configcontainer, SWT.NONE);
                gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
                gridData.grabExcessHorizontalSpace = true;
                gridData.grabExcessVerticalSpace = true;
                gAutoStop.setLayoutData(gridData);
                layout = new GridLayout();
                layout.numColumns = 2;
                gAutoStop.setLayout(layout);
                Messages.setLanguageText(gAutoStop, "autopilot.config.group.autostop");
                {

                    Group gTorrentDefaults = new Group(gAutoStop, SWT.NONE);
                    gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
                    gridData.horizontalSpan = 2;
                    gridData.grabExcessHorizontalSpace = true; //test
                    gTorrentDefaults.setLayoutData(gridData);
                    layout = new GridLayout();
                    layout.numColumns = 3;
                    gTorrentDefaults.setLayout(layout);
                    Messages.setLanguageText(gTorrentDefaults, "autopilot.config.group.torrentdefaults");
                    {
                        label = new Label(gTorrentDefaults, SWT.NONE);
                        Messages.setLanguageText(label, "autopilot.config.defaultmaxtype");
                        final String activeMaxRatioLabels[] = new String[3];
                        final int    activeMaxRatioValues[] = new int[3];
                        activeMaxRatioLabels[0] = this.plugin.getInterface().getUtilities().getLocaleUtilities().getLocalisedMessageText("autopilot.config.ratiotype.unlimited");
                        activeMaxRatioValues[0] = -1;
                        activeMaxRatioLabels[1] = this.plugin.getInterface().getUtilities().getLocaleUtilities().getLocalisedMessageText("autopilot.config.ratiotype.stopimmediatly");
                        activeMaxRatioValues[1] = 0;
                        activeMaxRatioLabels[2] = this.plugin.getInterface().getUtilities().getLocaleUtilities().getLocalisedMessageText("autopilot.config.ratiotype.userdefined");
                        activeMaxRatioValues[2] = 1;
                        gridData = new GridData();
                        gridData.horizontalSpan = 2;
                        new IntListParameter(gTorrentDefaults, "autopilot_as_defaultmaxratiotype", AutoPilot.CONFIG_DEFAULT_STOPMODE, activeMaxRatioLabels, activeMaxRatioValues).setLayoutData(gridData);

                        label = new Label(gTorrentDefaults, SWT.NONE);
                        Messages.setLanguageText(label, "autopilot.config.defaultmaxratio");
                        gridData = new GridData();
                        gridData.widthHint = 40;
                        gridData.horizontalSpan = 2;
                        new APFloatParameter(gTorrentDefaults, "autopilot_as_defaultmaxratio", Float.parseFloat(AutoPilot.CONFIG_DEFAULT_MAXRATIO), 0, -1, true).setLayoutData(gridData);

                        label = new Label(gTorrentDefaults, SWT.NONE);
                        Messages.setLanguageText(label, "autopilot.config.defaultminseedthreshold");
                        gridData = new GridData();
                        gridData.widthHint = 40;
                        gridData.horizontalSpan = 2;
                        new IntParameter(gTorrentDefaults, "autopilot_as_defaultminseedthreshold", AutoPilot.CONFIG_DEFAULT_MINSEEDTHRESH).setLayoutData(gridData);

                        label = new Label(gTorrentDefaults, SWT.NONE);
                        Messages.setLanguageText(label, "autopilot.config.defaultmaxseedthreshold");
                        gridData = new GridData();
                        gridData.widthHint = 40;
                        new IntParameter(gTorrentDefaults, "autopilot_as_defaultmaxseedthreshold", AutoPilot.CONFIG_DEFAULT_MAXSEEDTHRESH).setLayoutData(gridData);
                        new BooleanParameter(gTorrentDefaults, "autopilot_as_defaultmaxseedinclusive", AutoPilot.CONFIG_DEFAULT_MAXSEEDINCLUSIVE, "autopilot.config.defaultmaxseedinclusive");
                        
                        chkros = new BooleanParameter(gTorrentDefaults, "autopilot_as_removeonstop", AutoPilot.CONFIG_DEFAULT_REMOVEONSTOP, "autopilot.config.defaultremoveonstop");
                        chkros.getControl().addListener(SWT.Selection, new Listener() {
                            @Override
                            public void handleEvent(Event event) {
                                chkdos.getControl().setEnabled(chkros.isSelected());
                            }
                        });
                        gridData = new GridData();
                        gridData.horizontalSpan = 2;
                        chkdos = new BooleanParameter(gTorrentDefaults, "autopilot_as_deleteonstop", AutoPilot.CONFIG_DEFAULT_DELETEONSTOP, "autopilot.config.defaultdeleteonstop");
                        chkdos.setLayoutData(gridData);
                        chkdos.getControl().setEnabled(chkros.isSelected());

                        Group gTrackerDefaults = new Group(gTorrentDefaults, SWT.NONE);
                        gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
                        gridData.horizontalSpan = 3;
                        gridData.grabExcessHorizontalSpace = true;
                        gTrackerDefaults.setLayoutData(gridData);
                        layout = new GridLayout();
                        gTrackerDefaults.setLayout(layout);
                        Messages.setLanguageText(gTrackerDefaults, "autopilot.config.group.trackerdefaults");                    
                        {
                            lvTrackerDefaults = new Table(gTrackerDefaults, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
                            gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
                            gridData.grabExcessHorizontalSpace = true;
                            gridData.heightHint = 100;
                            lvTrackerDefaults.setLayoutData(gridData);
                            layout = new GridLayout();
                            lvTrackerDefaults.setLayout(layout);
                            lvTrackerDefaults.setHeaderVisible(true);

                            TableColumn tcURL = new TableColumn(lvTrackerDefaults, SWT.NONE);
                            tcURL.setText("Tracker URL");
                            tcURL.setWidth(140);
                            TableColumn tcMode = new TableColumn(lvTrackerDefaults, SWT.NONE);
                            tcMode.setAlignment(SWT.RIGHT);
                            tcMode.setText("Max Ratio");
                            tcMode.setWidth(70);
                            TableColumn tcMinSeed = new TableColumn(lvTrackerDefaults, SWT.NONE);
                            tcMinSeed.setAlignment(SWT.RIGHT);
                            tcMinSeed.setText("Min Seeds");
                            tcMinSeed.setWidth(70);
                            TableColumn tcMaxSeed = new TableColumn(lvTrackerDefaults, SWT.NONE);
                            tcMaxSeed.setAlignment(SWT.RIGHT);
                            tcMaxSeed.setText("Max Seeds");
                            tcMaxSeed.setWidth(70);
                            TableColumn tcFlags = new TableColumn(lvTrackerDefaults, SWT.NONE);
                            tcFlags.setAlignment(SWT.CENTER);
                            tcFlags.setText("Flags");
                            tcFlags.setWidth(70);

                            overrides.RegisterTable(lvTrackerDefaults);

                            Composite overridebuttons = new Composite(gTrackerDefaults, SWT.NULL);       
                            gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_END);
                            overridebuttons.setLayoutData(gridData);
                            layout = new GridLayout();
                            layout.numColumns = 2;
                            layout.marginHeight = 0;
                            layout.makeColumnsEqualWidth = false;
                            overridebuttons.setLayout(layout);
                            {
                                Button cmdRemove = new Button(overridebuttons, SWT.PUSH | SWT.CENTER);
                                Messages.setLanguageText(cmdRemove, "autopilot.dialog.mytorrents.button.remove");
                                gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
                                gridData.widthHint = 70;
                                cmdRemove.setLayoutData(gridData);
                                cmdRemove.addListener(SWT.Selection, new Listener() {
                                    @Override
                                    public void handleEvent(Event event) {
                                        TableItem[] selitem = lvTrackerDefaults.getSelection();

                                        for (int i = 0; i < selitem.length; i++) {
                                            try {
                                                overrides.Remove((String)selitem[i].getData());
                                            } catch(Exception ex) {
                                            }
                                        }
                                    }
                                });

                                Button cmdModify = new Button(overridebuttons, SWT.PUSH | SWT.CENTER);
                                Messages.setLanguageText(cmdModify, "autopilot.dialog.mytorrents.button.modify");
                                gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
                                gridData.widthHint = 70;
                                cmdModify.setLayoutData(gridData);
                                cmdModify.addListener(SWT.Selection, new Listener() {
                                    @Override
                                    public void handleEvent(Event event) {
                                        TableItem[] selitem = lvTrackerDefaults.getSelection();

                                        for (int i = 0; i < selitem.length; i++) {
                                            try {
                                                new AutoPilotRatioDialog(AutoPilotConfig.this.plugin, AutoPilotConfig.this.overrides, (String)selitem[i].getData());
                                            } catch(Exception ex) {
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }

                    gridData = new GridData();
                    gridData.horizontalSpan = 2;
                    new BooleanParameter(gAutoStop, "autopilot_as_stoponqueue", AutoPilot.CONFIG_DEFAULT_STOPONQUEUE, "autopilot.config.stoponqueue").setLayoutData(gridData);

                    gridData = new GridData();
                    gridData.horizontalSpan = 2;
                    new BooleanParameter(gAutoStop, "autopilot_as_skipforcestarts", AutoPilot.CONFIG_DEFAULT_SKIPFORCESTART, "autopilot.config.skipforcestarts").setLayoutData(gridData);

                    gridData = new GridData();
                    gridData.horizontalSpan = 2;
                    new BooleanParameter(gAutoStop, "autopilot_as_alertonstop", AutoPilot.CONFIG_DEFAULT_ALERTONSTOP, "autopilot.config.alertonstop").setLayoutData(gridData);

                    label = new Label(gAutoStop, SWT.NONE);
                    Messages.setLanguageText(label, "autopilot.config.pollinginterval");
                    gridData = new GridData();
                    gridData.widthHint = 40;
                    new IntParameter(gAutoStop, "autopilot_as_interval", AutoPilot.CONFIG_DEFAULT_UPDATEFREQUENCY).setLayoutData(gridData);
                }

                Composite logocontainer = new Composite(configcontainer, SWT.NONE);
                gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
                gridData.horizontalAlignment = GridData.CENTER;
                logocontainer.setLayoutData(gridData);
                layout = new GridLayout();
                layout.numColumns = 2;
                logocontainer.setLayout(layout);
                {
                    //Load my custom logo for the properties page =)
                    CLabel cLabel1 = new CLabel(logocontainer, SWT.NONE);
                    gridData = new GridData();
                    gridData.verticalSpan = 3;
                    cLabel1.setLayoutData(gridData);               
                    ClassLoader loader = AutoPilotConfig.class.getClassLoader();
                    InputStream is = loader.getResourceAsStream("com/tdelta/azureus/plugins/autopilot/UI/logo-small-transparancy.png");
                    if (is != null) {
                        Image img = new Image(logocontainer.getDisplay(), is);
                        cLabel1.setImage(img);
                    }

                    label = new Label(logocontainer, SWT.NONE);
                    label.setText("3D Delta Developers");
                    gridData = new GridData();
                    gridData.horizontalAlignment = GridData.CENTER;
                    label.setLayoutData(gridData);
                    Font logofont = new Font(logocontainer.getDisplay(),"Arial",14,FontUIResource.BOLD);
                    label.setFont(logofont);

                    label = new Label(logocontainer, SWT.NONE);
                    label.setText("http://www.3ddelta.com/");
                    gridData = new GridData();
                    gridData.horizontalAlignment = GridData.CENTER;
                    label.setLayoutData(gridData);

                    label = new Label(logocontainer, SWT.NONE);
                    label.setText(plugin.getInterface().getPluginName() + " " + plugin.getInterface().getPluginVersion());
                    gridData = new GridData();
                    gridData.horizontalAlignment = GridData.CENTER;
                    label.setLayoutData(gridData);
                }
            }
        } catch(Exception ex) {
            configcontainer = new Composite(parent, SWT.NULL);       
            gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
            configcontainer.setLayoutData(gridData);
            layout = new GridLayout();
            layout.marginHeight = 0;
            configcontainer.setLayout(layout);
            {
                label = new Label(configcontainer, SWT.NONE);
                label.setText("### An error occurred while generating configuration panel ###");
            }
            plugin.writelog("Exception thrown while creating configuration section: " + ex.getMessage());
        }
        
        return configcontainer;
    }

    @Override
    public void configSectionSave() {
    }

    @Override
    public void configSectionDelete() {
    }
}
