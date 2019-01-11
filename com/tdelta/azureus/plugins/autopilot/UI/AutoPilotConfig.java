package com.tdelta.azureus.plugins.autopilot.UI;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.pif.UISWTParameterContext;
import com.tdelta.azureus.plugins.autopilot.AutoPilot;
import com.tdelta.azureus.plugins.autopilot.TrackerOverrides;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.ui.config.BooleanParameter;
import com.biglybt.pif.ui.config.Parameter;
import com.biglybt.pif.ui.config.ParameterGroup;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;
import com.biglybt.pif.utils.LocaleUtilities;

/**
 * AutoPilot's SWT UI Configuration Panel
 *
 * @author clewis
 */
public class AutoPilotConfig
{

	public AutoPilotConfig(AutoPilot plugin, PluginInterface pi,
	                       TrackerOverrides overrides) {

		LocaleUtilities locale = pi.getUtilities().getLocaleUtilities();
		Parameter param;

		BasicPluginConfigModel model = pi.getUIManager().createBasicPluginConfigModel(
				"autopilot.name");

		List<Parameter> paramsAutoStop = new ArrayList<>();
		List<Parameter> paramsAutoStopNTD = new ArrayList<>();

		//

		final String[] activeMaxRatioLabels = new String[3];
		final int[] activeMaxRatioValues = new int[3];
		activeMaxRatioLabels[0] = locale.getLocalisedMessageText(
				"autopilot.config.ratiotype.unlimited");
		activeMaxRatioValues[0] = -1;
		activeMaxRatioLabels[1] = locale.getLocalisedMessageText(
				"autopilot.config.ratiotype.stopimmediatly");
		activeMaxRatioValues[1] = 0;
		activeMaxRatioLabels[2] = locale.getLocalisedMessageText(
				"autopilot.config.ratiotype.userdefined");
		activeMaxRatioValues[2] = 1;
		param = model.addIntListParameter2(
				unsafekey("autopilot_as_defaultmaxratiotype"),
				"autopilot.config.defaultmaxtype", activeMaxRatioValues,
				activeMaxRatioLabels, AutoPilot.CONFIG_DEFAULT_STOPMODE);
		paramsAutoStopNTD.add(param);

		//

		param = model.addFloatParameter2(unsafekey("autopilot_as_defaultmaxratio"),
				"autopilot.config.defaultmaxratio",
				Float.parseFloat(AutoPilot.CONFIG_DEFAULT_MAXRATIO), 0, -1, true, 1);
		paramsAutoStopNTD.add(param);

		//

		param = model.addIntParameter2(
				unsafekey("autopilot_as_defaultminseedthreshold"),
				"autopilot.config.defaultminseedthreshold",
				AutoPilot.CONFIG_DEFAULT_MINSEEDTHRESH);
		paramsAutoStopNTD.add(param);

		//

		Parameter paramMaxSeedThreshold = model.addIntParameter2(
				unsafekey("autopilot_as_defaultmaxseedthreshold"),
				"autopilot.config.defaultmaxseedthreshold",
				AutoPilot.CONFIG_DEFAULT_MAXSEEDTHRESH);

		//

		param = model.addBooleanParameter2(
				unsafekey("autopilot_as_defaultmaxseedinclusive"),
				"autopilot.config.defaultmaxseedinclusive",
				AutoPilot.CONFIG_DEFAULT_MAXSEEDINCLUSIVE);

		ParameterGroup pgMaxSeed = model.createGroup(null, new Parameter[] {
			paramMaxSeedThreshold,
			param
		});
		pgMaxSeed.setNumberOfColumns(2);
		paramsAutoStopNTD.add(pgMaxSeed);

		//

		BooleanParameter chkros = model.addBooleanParameter2(
				unsafekey("autopilot_as_removeonstop"),
				"autopilot.config.defaultremoveonstop",
				AutoPilot.CONFIG_DEFAULT_REMOVEONSTOP);

		BooleanParameter chkdos = model.addBooleanParameter2(
				unsafekey("autopilot_as_deleteonstop"),
				"autopilot.config.defaultdeleteonstop",
				AutoPilot.CONFIG_DEFAULT_DELETEONSTOP);

		chkros.addEnabledOnSelection(chkdos);

		ParameterGroup pgOnStop = model.createGroup(null, new Parameter[] {
			chkros,
			chkdos
		});
		pgOnStop.setNumberOfColumns(2);
		paramsAutoStopNTD.add(pgOnStop);

		//

		param = model.addUIParameter2(new paramTrackerOverrides(overrides, plugin),
				null);
		paramsAutoStopNTD.add(param);

		//

		param = model.addBooleanParameter2(unsafekey("autopilot_as_stoponqueue"),
				"autopilot.config.stoponqueue", AutoPilot.CONFIG_DEFAULT_STOPONQUEUE);
		paramsAutoStop.add(param);

		//

		param = model.addBooleanParameter2(
				unsafekey("autopilot_as_skipforcestarts"),
				"autopilot.config.skipforcestarts",
				AutoPilot.CONFIG_DEFAULT_SKIPFORCESTART);
		paramsAutoStop.add(param);

		//

		param = model.addBooleanParameter2(unsafekey("autopilot_as_alertonstop"),
				"autopilot.config.alertonstop", AutoPilot.CONFIG_DEFAULT_ALERTONSTOP);
		paramsAutoStop.add(param);

		//

		param = model.addIntParameter2(unsafekey("autopilot_as_interval"),
				"autopilot.config.pollinginterval",
				AutoPilot.CONFIG_DEFAULT_UPDATEFREQUENCY);
		paramsAutoStop.add(param);

		//

		ParameterGroup groupNTD = model.createGroup(
				"autopilot.config.group.torrentdefaults",
				paramsAutoStopNTD.toArray(new Parameter[0]));
		paramsAutoStop.add(groupNTD);

		ParameterGroup groupAutoStop = model.createGroup(
				"autopilot.config.group.autostop",
				paramsAutoStop.toArray(new Parameter[0]));

		//

		model.addUIParameter2(new ParamLogoArea(pi), null);
	}

	private String unsafekey(String key) {
		// Originally, AutoPilot stored settings directly to config (no plugin key prefix)
		// Wrapping in "!" tells core to use unsafe, raw key
		return "!" + key + "!";
	}

	private static class ParamLogoArea
		implements UISWTParameterContext
	{
		private final PluginInterface pi;

		public ParamLogoArea(PluginInterface pi) {
			this.pi = pi;
		}

		@Override
		public void create(Composite c) {
			c.setLayout(new GridLayout());
			Composite logocontainer = new Composite(c, SWT.NONE);
			GridData gridData = new GridData(
					GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
			gridData.horizontalAlignment = GridData.CENTER;
			logocontainer.setLayoutData(gridData);
			GridLayout layout = new GridLayout(2, false);
			logocontainer.setLayout(layout);
			{
				//Load my custom logo for the properties page =)
				CLabel cLabel1 = new CLabel(logocontainer, SWT.NONE);
				gridData = new GridData();
				gridData.verticalSpan = 3;
				cLabel1.setLayoutData(gridData);
				ClassLoader loader = AutoPilotConfig.class.getClassLoader();
				InputStream is = loader.getResourceAsStream(
						"com/tdelta/azureus/plugins/autopilot/UI/logo-small-transparancy.png");
				if (is != null) {
					Image img = new Image(logocontainer.getDisplay(), is);
					cLabel1.setImage(img);
				}

				Label label = new Label(logocontainer, SWT.NONE);
				label.setText("3D Delta Developers");
				gridData = new GridData();
				gridData.horizontalAlignment = GridData.CENTER;
				label.setLayoutData(gridData);
				Font logofont = new Font(logocontainer.getDisplay(), "Arial", 14,
						SWT.BOLD);
				label.setFont(logofont);

				label = new Label(logocontainer, SWT.NONE);
				label.setText("http://www.3ddelta.com/");
				gridData = new GridData();
				gridData.horizontalAlignment = GridData.CENTER;
				label.setLayoutData(gridData);

				label = new Label(logocontainer, SWT.NONE);
				label.setText(pi.getPluginName() + " " + pi.getPluginVersion());
				gridData = new GridData();
				gridData.horizontalAlignment = GridData.CENTER;
				label.setLayoutData(gridData);
			}

		}
	}

	private static class paramTrackerOverrides
		implements UISWTParameterContext
	{
		private final TrackerOverrides overrides;

		private final AutoPilot plugin;

		public paramTrackerOverrides(TrackerOverrides overrides, AutoPilot plugin) {
			this.overrides = overrides;
			this.plugin = plugin;
		}

		@Override
		public void create(Composite c) {
			c.setLayout(new GridLayout(2, false));
			Group gTrackerDefaults = new Group(c, SWT.NONE);
			GridData gridData = new GridData(
					GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
			gridData.horizontalSpan = 3;
			gridData.grabExcessHorizontalSpace = true;
			gTrackerDefaults.setLayoutData(gridData);
			GridLayout layout = new GridLayout();
			gTrackerDefaults.setLayout(layout);
			Messages.setLanguageText(gTrackerDefaults,
					"autopilot.config.group.trackerdefaults");
			{
				Table lvTrackerDefaults = new Table(gTrackerDefaults,
						SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
				gridData = new GridData(
						GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
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
				gridData = new GridData(
						GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_END);
				overridebuttons.setLayoutData(gridData);
				layout = new GridLayout();
				layout.numColumns = 2;
				layout.marginHeight = 0;
				layout.makeColumnsEqualWidth = false;
				overridebuttons.setLayout(layout);
				{
					Button cmdRemove = new Button(overridebuttons, SWT.PUSH | SWT.CENTER);
					Messages.setLanguageText(cmdRemove,
							"autopilot.dialog.mytorrents.button.remove");
					gridData = new GridData(
							GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
					gridData.widthHint = 70;
					cmdRemove.setLayoutData(gridData);
					cmdRemove.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event event) {
							TableItem[] selitem = lvTrackerDefaults.getSelection();

							for (int i = 0; i < selitem.length; i++) {
								try {
									overrides.Remove((String) selitem[i].getData());
								} catch (Exception ex) {
								}
							}
						}
					});

					Button cmdModify = new Button(overridebuttons, SWT.PUSH | SWT.CENTER);
					Messages.setLanguageText(cmdModify,
							"autopilot.dialog.mytorrents.button.modify");
					gridData = new GridData(
							GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
					gridData.widthHint = 70;
					cmdModify.setLayoutData(gridData);
					cmdModify.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event event) {
							TableItem[] selitem = lvTrackerDefaults.getSelection();

							for (int i = 0; i < selitem.length; i++) {
								try {
									new AutoPilotRatioDialog(plugin, overrides,
											(String) selitem[i].getData());
								} catch (Exception ex) {
								}
							}
						}
					});
				}
			}
		}
	}
}
