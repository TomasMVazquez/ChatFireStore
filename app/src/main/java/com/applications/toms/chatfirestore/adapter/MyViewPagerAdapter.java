package com.applications.toms.chatfirestore.adapter;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class MyViewPagerAdapter extends FragmentStatePagerAdapter {

    //Atributos
    private List<Fragment> fragmentList;
    private List<String> titulos;

    //constructor
    public MyViewPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titulos) {
        super(fm);
        this.fragmentList = fragmentList;
        this.titulos = titulos;
    }

    //setter
    public void setFragmentList(List<Fragment> fragmentList) {
        this.fragmentList = fragmentList;
        notifyDataSetChanged();
    }

    public void setTitulos(List<String> titulos) {
        this.titulos = titulos;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    //Metodo que usa el tabLayout para pedir el titulo de las pages
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

       switch (position){
            case 0:
                //return "Rojo";
            case 1:
                //return "Verde";
            case 2:
                //return "Azul";
        }

        return titulos.get(position);

    }

}