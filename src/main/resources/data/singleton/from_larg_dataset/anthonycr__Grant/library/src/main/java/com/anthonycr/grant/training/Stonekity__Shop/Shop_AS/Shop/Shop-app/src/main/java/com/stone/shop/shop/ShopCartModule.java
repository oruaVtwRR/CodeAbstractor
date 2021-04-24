package com.stone.shop.shop;

import android.content.Intent;
import android.util.Log;

import com.stone.shop.shop.ui.activity.PayOrderActivity;
import com.stone.shop.base.application.BaseApplication;
import com.stone.shop.shop.model.Order;
import com.stone.shop.user.model.User;
import com.stone.shop.base.util.LocalBroadcasts;
import com.stone.shop.base.util.ToastUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 购物车模块
 * <p/>
 * Created by stonekity.shi on 2015/1/20.
 */
public class ShopCartModule {

    private static final String TAG = "ShopCartModule";

    public static final String KEY_EXTRA_INIT_PAY_ORDER_RESULT = "com.stone.shop.KEY_EXTRA_INIT_PAY_ORDER_RESULT";
    public static final String KEY_EXTRA_PAY_ORDER_RESULT = "com.stone.shop.KEY_EXTRA_PAY_ORDER_RESULT";

    private static ShopCartModule instance = new ShopCartModule();
    private static Object lock = new Object();

    /**
     * 购物车中的物品
     */
    private List<Order> shopCartList;

    private ShopCartObservable observable = new ShopCartObservable();

    private ShopCartModule() {
        shopCartList = new ArrayList<>();
        // 获取当前用户购物车中所有的商品
        findAll();
    }

    public static ShopCartModule getInstance() {
        return instance;
    }


    /**
     * 获取购物车中商品数量
     *
     * @return
     */
    public int getCount() {
        if (null == shopCartList) {
            return 0;
        }

        return shopCartList.size();
    }

    /**
     * 获取当前订单数量
     *
     * @return
     */
    public int getCurOrderCount() {
        if (null == shopCartList)
            return 0;

        int count = 0;
        Iterator<Order> iterator = shopCartList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getStateCode() == Order.ORDER_STATE_CODE_IN_PAY)
                count++;
        }

        return count;
    }


    /**
     * 获取购物车中订单数量
     *
     * @return
     */
    public int getCarOrderCount() {
        if (null == shopCartList)
            return 0;
        int count = 0;
        Iterator<Order> iterator = shopCartList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getStateCode() == Order.ORDER_STATE_CODE_IN_BAG)
                count++;
        }

        return count;
    }


    /**
     * 添加商品到购物车
     *
     * @param object
     * @return
     */
    public synchronized void add(Order object) {
        if (null == shopCartList || null == object) {
            return;
        }

        synchronized (object) {
            Order mergedOrger = megerOrder(object);
            if (null != mergedOrger) {
                update(mergedOrger);
            } else {
                save(object);
            }
        }
    }

    /**
     * 合并订单
     *
     * @return
     */
    private Order megerOrder(Order order) {
        List<Order> list = ShopCartModule.getInstance().getAll();
        if (list == null || list.size() == 0)
            return null;
        Iterator<Order> iterator = list.iterator();
        while (iterator.hasNext()) {
            Order temp = iterator.next();
            String orderGoodId = order.getGood().getObjectId();
            String tempGoodId = temp.getGood().getObjectId();
            if (tempGoodId.equals(orderGoodId)) {
                int count = temp.getCount() + order.getCount();
                temp.setCount(count);

                if (temp.getGood() != null) {
                    double price = Double.parseDouble(temp.getGood().getPrice());
                    temp.setCost(count * price);
                }
                return temp;
            }
        }

        return null;
    }


    /**
     * 移除购物车中的某件商品
     *
     * @param order
     * @return
     */
    public synchronized void remove(Order order) {
        if (null == shopCartList || null == order || !shopCartList.contains(order)) {
            return;
        }

        synchronized (lock) {
            delete(order);
        }
    }


    /**
     * 获取购物车中所有的商品
     *
     * @return
     */
    public synchronized List<Order> getAll() {
        return shopCartList;
    }


    /**
     * 刷新购物车
     */
    public void refresh() {
        findAll();
    }


    /**
     * 从服务器获取购物车中所有订单
     */
    private void findAll() {

        if (shopCartList == null)
            shopCartList = new ArrayList<>();

        Log.d(TAG, "从服务器获取购物车中的商品");

        shopCartList.clear();
        User user = BmobUser.getCurrentUser(BaseApplication.getAppContext(), User.class);
        BmobQuery<Order> query = new BmobQuery<>();
        query.addWhereEqualTo("user", user);
        query.addWhereEqualTo("stateCode", Order.ORDER_STATE_CODE_IN_BAG);
        query.addWhereEqualTo("state", Order.ORDER_STATE_IN_BAG);
        query.include("good.shop");
        query.order("-updatedAt, -createdAt, state");
        query.findObjects(BaseApplication.getAppContext(), new FindListener<Order>() {
            @Override
            public void onSuccess(List<Order> orders) {
                ToastUtils.showToast("查询到购物车中共有 " + orders.size() + " 个商品");
                shopCartList = orders;
                observable.notifyChanged();
            }

            @Override
            public void onError(int i, String s) {
                observable.notifyChanged();
                ToastUtils.showToast("购物车获取失败，请稍后再试");
            }
        });

    }


    /**
     * 服务器 购物车添加
     *
     * @param order
     */
    private void save(final Order order) {
        if (null == order)
            return;
        order.save(BaseApplication.getAppContext(), new SaveListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, order.getGood().getName() + " 成功添加到购物车");
                ToastUtils.showToast("添加成功");
                shopCartList.add(order);
                observable.notifyChanged();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.showToast("购物车添加失败");
            }
        });
    }


    /**
     * 更新订单
     *
     * @param order
     */
    private void update(final Order order) {
        if (null == order)
            return;
        order.update(BaseApplication.getAppContext(), new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, order.getGood().getName() + " 成功添加到购物车");
                ToastUtils.showToast("添加成功，自动合并订单");

                //重新获取订单
                findAll();
                //observable.notifyChanged();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.showToast("订单合并失败");
            }
        });
    }

    /**
     * 批量更新购物车中订单状态
     *
     * @param list 购物车中订单列表
     * @param callback 是否需要发送广播
     */
    public void batchUpdateOrders(List<BmobObject> list, final boolean callback) {
        if (list == null || list.isEmpty())
            return;

        // 批量更新购物车中订单状态
        new BmobObject().updateBatch(BaseApplication.getAppContext(), list, new UpdateListener() {

            @Override
            public void onSuccess() {

                if(callback) {
                    Intent intent = new Intent();
                    intent.setAction(PayOrderActivity.RECEIVER_BATCH_UPDATE_ORDERS_RESULT);
                    intent.putExtra(KEY_EXTRA_INIT_PAY_ORDER_RESULT, true);
                    LocalBroadcasts.sendLocalBroadcast(intent);
                } else {
                    // 发送广播 结算订单成功
                    /*intent.setAction(PayOrderActivity.RECEIVER_PAY_ORDER_RESULT);
                    intent.putExtra(KEY_EXTRA_PAY_ORDER_RESULT, true);*/
                }

                // 更新购物车
                findAll();
            }

            @Override
            public void onFailure(int i, String s) {

                ToastUtils.showToast("批量更新购物车订单状态失败");

                if(callback) {
                    Intent intent = new Intent();
                    intent.setAction(PayOrderActivity.RECEIVER_BATCH_UPDATE_ORDERS_RESULT);
                    intent.putExtra(KEY_EXTRA_INIT_PAY_ORDER_RESULT, false);
                    LocalBroadcasts.sendLocalBroadcast(intent);
                } else {
                    // 发送广播 结算订单失败
                    /*intent.setAction(PayOrderActivity.RECEIVER_PAY_ORDER_RESULT);
                    intent.putExtra(KEY_EXTRA_PAY_ORDER_RESULT, false);*/
                }

                // 更新购物车
                findAll();

            }
        });

    }


    /**
     * 服务器 购物车移除
     *
     * @param order
     */
    private void delete(final Order order) {
        if (null == order)
            return;
        order.setState(Order.ORDER_STATE_OUT_BAG);
        order.update(BaseApplication.getAppContext(), new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, order.getGood().getName() + " 成功从购物车移除");
                ToastUtils.showToast("购物车成功移除");
                shopCartList.remove(order);
                observable.notifyChanged();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.showToast("购物车移除失败");
            }
        });
    }


    public void registerObserver(Observer observer) {
        if (null != observer) {
            observable.addObserver(observer);
        }
    }

    public void unregisterObserver(Observer observer) {
        observable.deleteObserver(observer);
    }


    public class ShopCartObservable extends Observable {

        public void notifyChanged() {
            setChanged();
            notifyObservers();
        }
    }

}
