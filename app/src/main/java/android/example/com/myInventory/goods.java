package android.example.com.myInventory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class goods {
    private String type;
    private float money_unit;
    private float amount;
    private float money;
    private String danwei;

    public goods(String _type, float _money_unit, float _amount, String _danwei) {
        this.type = _type;
        this.money_unit = _money_unit;
        this.amount = _amount;
        this.money = new BigDecimal(money_unit * amount).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        this.danwei = _danwei;
    }

    public String getType() {
        return type;
    }

    public String getDanwei() {
        return danwei;
    }

    public float getMoney_unit() {
        return money_unit;
    }

    public float getAmount() {
        return amount;
    }

    public float getMoney() {
        return money;
    }

    public static goods toGood(String json) {
        return JSONObject.parseObject(json, goods.class);
    }

    public static String toJson(Object good) {
        if (good == null) return null;
        return JSON.toJSONString(good, SerializerFeature.WriteNullStringAsEmpty);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public void setMoney_unit(float money_unit) {
        this.money_unit = money_unit;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setDanwei(String danwei) {
        this.danwei = danwei;
    }

    public static ArrayList<goods> toGoods(String json) {
        List mGoods = JSONObject.parseArray(json, goods.class);
        return (ArrayList) mGoods;
    }

    public goods() {
        super();
    }
}
