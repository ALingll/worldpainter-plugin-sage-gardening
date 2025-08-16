package org.cti.wpplugin.myplants.variable;

import org.cti.wpplugin.layers.editors.gui.PercentageItem;
import org.cti.wpplugin.layers.editors.gui.ValueEditor;

import java.util.Random;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-14 04:49
 **/
public class ProbabilityVar extends AbstractIntVar implements UiVariable{
    private Variable<Float> percentage = new Variable<>(null);
    private String desc = null;
    public ProbabilityVar(float percent){
        this(percent,null);
    }
    public ProbabilityVar(float percent,String desc) {
        super(0);
        setPercentage(percent);
        this.desc = desc;
    }

    public void setPercentage(float percentage) {
        if(percentage<0) {
            this.percentage.setValue(0f);
            return;
        }
        if(percentage>100) {
            this.percentage.setValue(100f);
            return;
        }
        this.percentage.setValue(percentage);
    }

    public float getPercentage(){return percentage.getValue();}

    @Override
    public Integer random(Random random) {
        variable = random.nextFloat() < (percentage.getValue() / 100.0f) ? 1 : 0;
        return variable;
    }

    @Override
    public ValueEditor getComponent() {
        return new PercentageItem(percentage);
    }

    @Override
    public void setDesc(String s) {
        this.desc = s;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public Object copyFrom(Object uiVariable) {
        this.percentage.variable = ((Variable<Float>)uiVariable).variable;
        return this.percentage;
    }

    public Variable<Float> getVar(){return percentage;}

    @Override
    public String toString(){
        return variable+"@"+Integer.toHexString(System.identityHashCode(this))+"{"
                +percentage
                +"}";
    }
}
