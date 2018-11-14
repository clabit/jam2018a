package com.novato.jam.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.novato.jam.R;

import java.util.ArrayList;

/**
 * Created by poshaly on 2017. 3. 29..
 */

public class FragmentAppCompatManager {
    private AppCompatActivity activity;
    private Fragment fragment;

    private int main_view_id;

    private android.support.v4.app.FragmentManager mFragmentManager;
    private String mLastFragmentName;



    public FragmentAppCompatManager(AppCompatActivity activity, int main_view_id){
        this.activity = activity;
        this.main_view_id = main_view_id;

        set();
    }

    public FragmentAppCompatManager(Fragment fragment, int main_view_id){
//        this.activity = activity;

        this.fragment = fragment;
        this.main_view_id = main_view_id;

        set();
    }

    private void set(){
        if(mFragmentManager == null){
            if(fragment!=null) {
                mFragmentManager = fragment.getChildFragmentManager();
            }
            else {
                mFragmentManager = activity.getSupportFragmentManager();
            }
        }
    }

    public void addFragment(Fragment newFragment){
        addFragment(newFragment, false);
    }
    public void addFragment(Fragment newFragment, boolean animation)
    {

//        LoggerManager.e("mun", "addFragment start: " +mFragmentManager.getBackStackEntryCount() );

        String name = newFragment.getClass().getName();
//        if (!TextUtils.isEmpty(name) && name.equals(mLastFragmentName)) {
//            replaceFragment(newFragment, animation);
//            return;
//        }
        mLastFragmentName = name;
//        layout_main.setVisibility(View.GONE);
//        layout_fragment.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if(animation)transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_right_out);
        transaction.addToBackStack(null);
        transaction.add(main_view_id, newFragment);
        transaction.commitAllowingStateLoss();

//        LoggerManager.e("mun", "addFragment end: " +mFragmentManager.getBackStackEntryCount() );
    }

    public void addFragmentTopAnimation(Fragment newFragment)
    {
        String name = newFragment.getClass().getName();
        mLastFragmentName = name;

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.setCustomAnimations(R.anim.push_top_in, R.anim.push_bottom_out);
        transaction.addToBackStack(null);
        transaction.add(main_view_id, newFragment);
        transaction.commitAllowingStateLoss();
    }
    public void replaceFragmentTopAnimation(Fragment newFragment)
    {

        String name = newFragment.getClass().getName();
        mLastFragmentName = name;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.push_top_in, R.anim.push_bottom_out);
        transaction.replace(main_view_id, newFragment);
        transaction.commitAllowingStateLoss();
    }


    public void replaceFragment(Fragment newFragment)
    {
        replaceFragment(newFragment, false);
    }
    public void replaceFragment(Fragment newFragment, boolean animation)
    {

//        LoggerManager.e("mun", "replaceFragment start: " +mFragmentManager.getBackStackEntryCount() );

        String name = newFragment.getClass().getName();
//        if (!TextUtils.isEmpty(name) && name.equals(mLastFragmentName))
//            return;
        mLastFragmentName = name;
//        layout_main.setVisibility(View.GONE);
//        layout_fragment.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if(animation)transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_right_out);
//        transaction.addToBackStack(mLastFragmentName);
//        transaction.addToBackStack(null);
        transaction.replace(main_view_id, newFragment);
//        transaction.add(main_view_id, newFragment);

        transaction.commitAllowingStateLoss();


//        LoggerManager.e("mun", "replaceFragment end: " +mFragmentManager.getBackStackEntryCount() );
    }

    public boolean popFragment()
    {
        return popFragment(false);
    }
    public boolean popFragment(boolean animation)
    {
        int count = mFragmentManager.getBackStackEntryCount();

//        LoggerManager.e("mun", "getBackStackEntryCount start: " +count );

        if (count <=0){
            return false;
        }

//        mFragmentManager.findFragmentById(main_view_id).getFragmentManager().popBackStack();
//        mFragmentManager.popBackStack(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        mFragmentManager.popBackStack();

        remove(animation);

        mLastFragmentName = "";


//        LoggerManager.e("mun", "getBackStackEntryCount end: " +mFragmentManager.getBackStackEntryCount() );

        return true;
    }

    public void removeAllFragments()
    {
        LoggerManager.e("mun", "getBackStackEntryCount start: " +mFragmentManager.getBackStackEntryCount() );

        try {
//        activity.getSupportFragmentManager().popBackStackImmediate(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mFragmentManager.popBackStackImmediate(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mLastFragmentName = "";
        }catch (Exception e){}

        remove();

//        mFragmentManager.popBackStack(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        mFragmentManager.popBackStack();


//        for (int i = 0; i < count; i++) {
//            mFragmentManager.popBackStack();
//        }
        LoggerManager.e("mun", "getBackStackEntryCount end: " +mFragmentManager.getBackStackEntryCount() );

    }
    public int childFragmentCount(){
        if(mFragmentManager!=null) {
            int count = mFragmentManager.getBackStackEntryCount();
            LoggerManager.e("mun", "getBackStackEntryCount: " + count);
            return count;
        }
        else{
            return 0;
        }
    }

    public ArrayList<Fragment> childFragments(){
        ArrayList<Fragment> list = new ArrayList<>();
        if(mFragmentManager!=null) {
            list.addAll(mFragmentManager.getFragments());
        }
        return list;
    }

    public void remove(){
        remove(false);
    }
    public void remove(boolean animation){
        try {
            Fragment f = mFragmentManager.findFragmentById(main_view_id);
            if (f != null && mFragmentManager != null) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                if (animation)
                    transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_right_out);
                transaction.remove(f).commit();
            }
        }catch (Exception e){}
        try{
            mFragmentManager.popBackStackImmediate();
        }catch (Exception e){}
    }

    public void removeAllFragments(Fragment remove)
    {
        int count = mFragmentManager.getBackStackEntryCount();
        LoggerManager.e("mun", "getBackStackEntryCount: " +count );

//        activity.getSupportFragmentManager().popBackStackImmediate(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        mFragmentManager.popBackStackImmediate(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        mLastFragmentName = "";


        if(remove!=null && mFragmentManager!=null)mFragmentManager.beginTransaction().remove(remove);

//        mFragmentManager.popBackStack(main_view_id, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        mFragmentManager.popBackStack();


//        for (int i = 0; i < count; i++) {
//            mFragmentManager.popBackStack();
//        }
    }
}
