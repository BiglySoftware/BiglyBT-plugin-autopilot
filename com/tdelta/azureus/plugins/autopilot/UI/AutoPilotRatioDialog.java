package com.tdelta.azureus.plugins.autopilot.UI;

import com.biglybt.ui.swt.imageloader.ImageLoader;
import com.tdelta.azureus.plugins.autopilot.AutoPilot;
import com.tdelta.azureus.plugins.autopilot.TrackerOverrides;
import java.text.MessageFormat;
import java.util.Hashtable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.biglybt.pif.download.Download;
import com.biglybt.ui.swt.ImageRepository;
import com.biglybt.ui.swt.Messages;

/**
 * Popup dialog box that allows users to modify torrent settings
 *
 * @author clewis
 */
public class AutoPilotRatioDialog {
    private AutoPilot        plugin;
    private Download         download;
    private TrackerOverrides trackeroverrides;
    private String           URL;
    private int              iStopMode;
    private String           sMaxRatio;
    private int              iMaxSeedThreshold;
    private int              iMinSeedThreshold;
    private int              iFlags;

    Text   txtMaxRatio;
    Text   txtMaxSeedThreshold;
    Text   txtMinSeedThreshold;    
    Combo  lstStopMode;
    Button chkRemoveOnStop;
    Button chkDeleteOnStop;
    Button chkMaxSeedInclusive;
            
    final String optionlabels[] = new String[3];
    final int    optionvalues[] = new int[3];

    public AutoPilotRatioDialog(AutoPilot plugin, TrackerOverrides trackeroverrides, String URL) {
        this.plugin           = plugin;
        this.trackeroverrides = trackeroverrides;
        this.URL              = URL;
        
        if (plugin instanceof AutoPilot) {
            if (trackeroverrides instanceof TrackerOverrides && URL.length() > 0) {
                try {
                    Hashtable htData = trackeroverrides.Get(URL);
                    if (htData != null) {
                        try {
                            iStopMode         = Integer.parseInt((String)htData.get("trackermode"));
                        } catch(Exception ex) {
                            iStopMode         = 0;
                        } 
                        try {
                            sMaxRatio         = (String)htData.get("trackerratio");
                        } catch(Exception ex) {
                            sMaxRatio         = "0";
                        }
                        try {
                            iMinSeedThreshold = Integer.parseInt((String)htData.get("trackerminseeds"));
                        } catch(Exception ex) {
                            iMinSeedThreshold = 0;
                        }
                        try {
                            iMaxSeedThreshold = Integer.parseInt((String)htData.get("trackermaxseeds"));
                        } catch(Exception ex) {
                            iMaxSeedThreshold = 0;
                        }
                        try {
                            iFlags            = Integer.parseInt((String)htData.get("trackerbitfield"));
                        } catch(Exception ex) {
                            iFlags            = 0;
                        }

                        createdialog();
                    }
                } catch(Exception ex) {
                    plugin.writelog("Exception thown while initializing dialog: " + ex.getMessage());
                }
            }
        }
    }
    
    public AutoPilotRatioDialog(AutoPilot plugin, Download download) {
        this.plugin           = plugin;
        this.download         = download;

        if (plugin instanceof AutoPilot) {
            if (download instanceof Download) {
                try {
                    try {
                        iStopMode = Integer.parseInt(download.getAttribute(plugin.attributeAutoStopMode));
                    } catch(Exception ex) {
                        iStopMode = 0;
                    }
                    try {
                        sMaxRatio = download.getAttribute(plugin.attributeMaxShareRatio);
                    } catch(Exception ex) {
                        sMaxRatio = "0";
                    }
                    try {
                        iMinSeedThreshold = Integer.parseInt(download.getAttribute(plugin.attributeMinSeedThreshold));
                    } catch(Exception ex) {
                        iMinSeedThreshold = 0;
                    }
                    try {
                        iMaxSeedThreshold = Integer.parseInt(download.getAttribute(plugin.attributeMaxSeedThreshold));
                    } catch(Exception ex) {
                        iMaxSeedThreshold = 0;
                    }
                    try {
                        iFlags = Integer.parseInt(download.getAttribute(plugin.attributeOptionsBF));
                    } catch(Exception ex) {
                        iFlags = 0;
                    }

                    createdialog();
                } catch(Exception ex) {
                    plugin.writelog("Exception thown while initializing dialog: " + ex.getMessage());
                }
            }
        }
    }
    
    private int UpdateBitfield(int LastField) {
        int field = 0;

        if (chkRemoveOnStop.getSelection()) {
            LastField = LastField | AutoPilot.APOPT_REMOVE_ON_STOP;
        } else {
            LastField = (LastField | AutoPilot.APOPT_REMOVE_ON_STOP) - AutoPilot.APOPT_REMOVE_ON_STOP;
        }
        
        if (chkDeleteOnStop.getSelection()) {
            LastField = LastField | AutoPilot.APOPT_DELETE_ON_STOP;
        } else {
            LastField = (LastField | AutoPilot.APOPT_DELETE_ON_STOP) - AutoPilot.APOPT_DELETE_ON_STOP;
        }

        if (chkMaxSeedInclusive.getSelection()) {
            LastField = LastField | AutoPilot.APOPT_MAX_SEED_INCLUSIVE;
        } else {
            LastField = (LastField | AutoPilot.APOPT_MAX_SEED_INCLUSIVE) - AutoPilot.APOPT_MAX_SEED_INCLUSIVE;
        }

        return LastField;
    }
    
    private void createdialog() {
        GridLayout layout;
        GridData   data;
        Label      label;
        
        try {
            final Shell shell = new Shell();

            if (download instanceof Download) {
                shell.setText(MessageFormat.format(plugin.getLocalisedMessageText("autopilot.dialog.mytorrents.title"), new java.lang.Object[] {download.getName()}));
            } else {
                shell.setText(MessageFormat.format(plugin.getLocalisedMessageText("autopilot.dialog.mytorrents.title"), new java.lang.Object[] {URL}));
            }

            shell.setImage(ImageLoader.getInstance().getImage("logo16"));
            layout = new GridLayout();
            shell.setLayout(layout);
            data = new GridData();
            shell.setLayoutData(data);
            {
                Group gAutoStop = new Group(shell, SWT.NONE);
                layout = new GridLayout();
                layout.numColumns = 3;
                gAutoStop.setLayout(layout);
                data = new GridData();
                data.horizontalAlignment = GridData.FILL;
                data.verticalAlignment = GridData.FILL;
                data.grabExcessHorizontalSpace = true;
                data.grabExcessVerticalSpace = true;
                gAutoStop.setLayoutData(data);
                Messages.setLanguageText(gAutoStop, "autopilot.config.group.autostop");
                {
                    label = new Label(gAutoStop, SWT.NONE);
                    Messages.setLanguageText(label, "autopilot.dialog.mytorrents.stopmode");

                    lstStopMode = new Combo(gAutoStop, SWT.SINGLE | SWT.READ_ONLY);

                    optionlabels[0] = this.plugin.getLocalisedMessageText("autopilot.config.ratiotype.unlimited");
                    optionvalues[0] = AutoPilot.AP_UNLIMITED;
                    optionlabels[1] = this.plugin.getLocalisedMessageText("autopilot.config.ratiotype.stopimmediatly");
                    optionvalues[1] = AutoPilot.AP_AUTOSTOP;
                    optionlabels[2] = this.plugin.getLocalisedMessageText("autopilot.config.ratiotype.userdefined");
                    optionvalues[2] = AutoPilot.AP_USERRATIO;
                    lstStopMode.setItems(optionlabels);
                    lstStopMode.setData(optionvalues);
                    data = new GridData(GridData.FILL_HORIZONTAL);
                    data.horizontalSpan = 2;
                    lstStopMode.setLayoutData(data);
                    switch(iStopMode) {
                        case AutoPilot.AP_UNLIMITED:
                            lstStopMode.select(0);
                            break;
                        case AutoPilot.AP_AUTOSTOP:
                            lstStopMode.select(1);
                            break;
                        case AutoPilot.AP_USERRATIO:
                            lstStopMode.select(2);
                            break;
                    }


                    label = new Label(gAutoStop, SWT.NONE);
                    Messages.setLanguageText(label, "autopilot.dialog.mytorrents.maxratio");
                    txtMaxRatio = new Text(gAutoStop,SWT.BORDER);
                    txtMaxRatio.setText(sMaxRatio);
                    data = new GridData(GridData.FILL_HORIZONTAL);
                    data.horizontalSpan = 2;
                    txtMaxRatio.setLayoutData(data);

                    label = new Label(gAutoStop, SWT.NONE);
                    Messages.setLanguageText(label, "autopilot.dialog.mytorrents.minseedthreshold");
                    txtMinSeedThreshold = new Text(gAutoStop,SWT.BORDER);
                    txtMinSeedThreshold.setText(Integer.toString(iMinSeedThreshold));
                    data = new GridData(GridData.FILL_HORIZONTAL);
                    data.horizontalSpan = 2;
                    txtMinSeedThreshold.setLayoutData(data);

                    label = new Label(gAutoStop, SWT.NONE);
                    Messages.setLanguageText(label, "autopilot.dialog.mytorrents.maxseedthreshold");
                    txtMaxSeedThreshold = new Text(gAutoStop,SWT.BORDER);
                    txtMaxSeedThreshold.setText(Integer.toString(iMaxSeedThreshold));
                    data = new GridData(GridData.FILL_HORIZONTAL);
                    txtMaxSeedThreshold.setLayoutData(data);
                    chkMaxSeedInclusive = new Button(gAutoStop, SWT.CHECK);
                    Messages.setLanguageText(chkMaxSeedInclusive, "autopilot.dialog.mytorrents.maxseedinclusive");
                    chkMaxSeedInclusive.setSelection((iFlags & AutoPilot.APOPT_MAX_SEED_INCLUSIVE) > 0);

                    chkRemoveOnStop = new Button(gAutoStop, SWT.CHECK);
                    Messages.setLanguageText(chkRemoveOnStop, "autopilot.dialog.mytorrents.removeonstop");
                    chkRemoveOnStop.setSelection((iFlags & AutoPilot.APOPT_REMOVE_ON_STOP) > 0);
                    chkRemoveOnStop.addSelectionListener(new SelectionListener() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            chkDeleteOnStop.setEnabled(chkRemoveOnStop.getSelection());
                        }
                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                        }
                    });

                    chkDeleteOnStop = new Button(gAutoStop, SWT.CHECK);
                    data = new GridData();
                    data.horizontalSpan = 2;
                    chkDeleteOnStop.setLayoutData(data);
                    Messages.setLanguageText(chkDeleteOnStop, "autopilot.dialog.mytorrents.deleteonstop");
                    chkDeleteOnStop.setSelection((iFlags & AutoPilot.APOPT_DELETE_ON_STOP) > 0);
                    chkDeleteOnStop.setEnabled(chkRemoveOnStop.getSelection());
                }

                Button cmdOK = new Button(shell, SWT.PUSH | SWT.CENTER);
                Messages.setLanguageText(cmdOK, "autopilot.dialog.mytorrents.button.ok");

                data = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
                data.widthHint = 70;
                cmdOK.setLayoutData(data);
                cmdOK.addListener(SWT.Selection, new Listener() {
                    @Override
                    public void handleEvent(Event event) {
                        try {
                            if (AutoPilotRatioDialog.this.download instanceof Download) {
                                download.setAttribute(plugin.attributeAutoStopMode, String.valueOf(optionvalues[lstStopMode.getSelectionIndex()]));
                                download.setAttribute(plugin.attributeMaxShareRatio, txtMaxRatio.getText());
                                download.setAttribute(plugin.attributeMaxSeedThreshold, txtMaxSeedThreshold.getText());
                                download.setAttribute(plugin.attributeMinSeedThreshold, txtMinSeedThreshold.getText());
                                download.setAttribute(plugin.attributeOptionsBF, Integer.toString(UpdateBitfield(iFlags)));
                                plugin.UpdateRatioColumn();
                            }
                            if (AutoPilotRatioDialog.this.trackeroverrides instanceof TrackerOverrides) {
                                trackeroverrides.Update(AutoPilotRatioDialog.this.URL, optionvalues[lstStopMode.getSelectionIndex()], Float.parseFloat(txtMaxRatio.getText()), Integer.parseInt(txtMinSeedThreshold.getText()), Integer.parseInt(txtMaxSeedThreshold.getText()), UpdateBitfield(iFlags));
                            }

                            if (plugin.DEBUG) {
                                plugin.writelog("DevMsg: shell dimensions: h: " + shell.getSize().x + " w: " + shell.getSize().y);
                            }
                        } catch(Exception ex) {
                            plugin.writelog("exception thrown while updating: " + ex.getMessage());
                        }

                        shell.close();
                    }
                });
            }

            shell.setSize(369,212);
            Rectangle rClient = shell.getDisplay().getBounds();
            Rectangle rDialog = shell.getBounds();
            shell.setLocation(((rClient.width / 2) - (rDialog.width / 2)), ((rClient.height / 2) - (rDialog.height / 2)));
            shell.open();
        } catch(Exception ex) {
            plugin.writelog("Exception thrown while creating dialog: " + ex.getMessage());
        }
    }

}
