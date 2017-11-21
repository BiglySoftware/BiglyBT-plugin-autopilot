package com.tdelta.azureus.plugins.autopilot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.biglybt.core.config.*;

//==================================================================================================================
// FloatParameter Override
//==================================================================================================================
// This is basically just a copy of the FloatParameter class, with a simple modification to allow for a default
// parameter. Im not sure why this is missing, but this override corrects the issue... at least in my plugin =)
//==================================================================================================================
public class APFloatParameter {
    Text inputField;
    float fMinValue = 0;
    float fMaxValue = -1;
    float fDefaultValue;
    int iDigitsAfterDecimal = 1;
    String sParamName;
    boolean allowZero = false;
    
    //==============================================================================================================
    // My override to accept a default value. Hopefully oliver fixes this soon =)
    // Also, why is iDigitsAfterDecimal never used? I removed this from my override since it is pointless
    //==============================================================================================================
    public APFloatParameter(Composite composite, final String name, float defaultValue) {
        fDefaultValue = defaultValue;
        initialize(composite, name);
    }

    public APFloatParameter(Composite composite, final String name, float defaultValue, float minValue, float maxValue, boolean allowZero) {
        fDefaultValue = defaultValue;
        initialize(composite, name);
        fMinValue = minValue;
        fMaxValue = maxValue;
        this.allowZero = allowZero;
    }

    //==============================================================================================================
    // Original Constructors
    //==============================================================================================================
    public APFloatParameter(Composite composite, final String name) {
        fDefaultValue = COConfigurationManager.getFloatParameter(name);
        initialize(composite,name);
    }

    public APFloatParameter(Composite composite, final String name, float minValue, float maxValue, boolean allowZero, int digitsAfterDecimal) {
        fDefaultValue = COConfigurationManager.getFloatParameter(name);
        initialize(composite,name);
        fMinValue = minValue;
        fMaxValue = maxValue;
        this.allowZero = allowZero;
        iDigitsAfterDecimal = digitsAfterDecimal;
    }
  
    //==============================================================================================================
    // Slightly modified initializer
    //==============================================================================================================
    public void initialize(Composite composite, final String name) {
        sParamName = name;
        inputField = new Text(composite, SWT.BORDER);

//--------------------------------------------------------------------------------------
// Since we cannot directly compare a NULL value with a type of float, we need to
// recreate how getFloatParameter works, which basically just stores the data in a
// string format and converts it to a float on the fly.
//----[ OLD METHOD ]--------------------------------------------------------------------
//      float value = COConfigurationManager.getFloatParameter(name);
//      inputField.setText(String.valueOf(value));
//----[ NEW METHOD ]--------------------------------------------------------------------
        String sDefaultValue = Float.toString(fDefaultValue);
        String sValue = COConfigurationManager.getStringParameter(name, sDefaultValue);
        inputField.setText(sValue);
//--------------------------------------------------------------------------------------
        
        inputField.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String text = e.text;
                char[] chars = new char[text.length()];
                text.getChars(0, chars.length, chars, 0);
                for (int i = 0; i < chars.length; i++) {
                    if ( !((chars[i] >= '0' && chars[i] <= '9') || chars[i] == '.') ) {
                        e.doit = false;
                        return;
                    }
                }
            }
        });

        inputField.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                try {
                    float val = Float.parseFloat(inputField.getText());
                    if (val < fMinValue) {
                        if (!(allowZero && val == 0)) {
                            val = fMinValue;
                        }
                    }
                    if (val > fMaxValue) {
                        if (fMaxValue > -1) {
                            val = fMaxValue;
                        }
                    }
                    COConfigurationManager.setParameter(name, val);
                } catch (Exception e) {
                }
            }
        });

        inputField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                try {
                    float val = Float.parseFloat(inputField.getText());
                    if (val < fMinValue) {
                        if (!(allowZero && val == 0)) {
                            inputField.setText(String.valueOf(fMinValue));
                            COConfigurationManager.setParameter(name, fMinValue);
                        }
                    }
                    if (val > fMaxValue) {
                        if (fMaxValue > -1) {
                            inputField.setText(String.valueOf(fMaxValue));
                            COConfigurationManager.setParameter(name, fMaxValue);
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
    }
  

    public void setLayoutData(Object layoutData) {
        inputField.setLayoutData(layoutData);
    }
  
    public Control getControl() {
        return(inputField);
    }
}
