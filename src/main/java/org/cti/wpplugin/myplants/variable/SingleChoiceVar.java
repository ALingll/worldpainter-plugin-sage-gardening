package org.cti.wpplugin.myplants.variable;

import java.util.List;
import java.util.Random;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 04:09
 **/
public class SingleChoiceVar extends RandomVariable<String> {
    private List<String> stringList;

    public SingleChoiceVar(String v, List<String> list) {
        super(v);
        this.stringList = list;
    }

    public SingleChoiceVar(List<String> list) {
        super("");
        this.stringList = list;
    }

    //随机被包装对象的值并更新新值
    @Override
    public String random(Random random) {
        if(stringList.size()==1) setValue(stringList.get(0));
        else setValue(stringList.get(random.nextInt(stringList.size())));
        return getValue();
    }
}
