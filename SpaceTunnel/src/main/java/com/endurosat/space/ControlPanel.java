package com.endurosat.space;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlPanel {
    public static void main(String[] args){
        ControlPanel panel = new ControlPanel(new AtomicInteger(),new AtomicInteger(),new AtomicBoolean(),new AtomicBoolean(),new AtomicInteger(2000));
    }
    public ControlPanel(AtomicInteger latency, AtomicInteger loss, AtomicBoolean connection,AtomicBoolean discreteComms,AtomicInteger bandwidth) {
        JFrame f=new JFrame();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        JButton b=new JButton("Switch to Discrete communication");
        JButton b1=new JButton("Switch to Continuous communication");
        JSpinner f2 = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
        JButton b2=new JButton("Change Latency");
        JSpinner f3 = new JSpinner(new SpinnerNumberModel(1,1,100,1));
        JButton b3=new JButton("Loose N Packets");
        JButton b4=new JButton("Break connection");
        JButton b5=new JButton("Make connection");
        JSpinner f6 = new JSpinner(new SpinnerNumberModel(bandwidth.get(),0,10000,1));
        JButton b6 = new JButton("Set Bandwidth in B/s");
        b.setBounds(0,0,600, 40);
        b1.setBounds(0,40,600,40);
        f2.setBounds(150,90,300, 40);
        b2.setBounds(0,130,600, 40);
        f3.setBounds(150,180,300, 40);
        b3.setBounds(0,220,600, 40);
        b4.setBounds(0,270,600, 40);
        b5.setBounds(0,310,600, 40);
        f6.setBounds(150,360,300,40);
        b6.setBounds(0,400,600,40);
        b.addActionListener(new booleanHandler(true,discreteComms));
        b1.addActionListener(new booleanHandler(false,discreteComms));
        b2.addActionListener(new intHandler(f2,latency));
        b3.addActionListener(new intHandler(f3,loss));
        b4.addActionListener(new booleanHandler(false,connection));
        b5.addActionListener(new booleanHandler(true,connection));
        b6.addActionListener(new intHandler(f6,bandwidth));


        f.add(b);
        f.add(f2);
        f.add(b1);
        f.add(f3);
        f.add(b2);
        f.add(b3);
        f.add(b4);
        f.add(b5);
        f.add(b6);
        f.add(f6);
        f.setSize(600,600);
        f.setLayout(null);
        f.setVisible(true);
    }

    public class intHandler implements ActionListener {
        JSpinner value;
        AtomicInteger target;
        public intHandler(JSpinner value,AtomicInteger target){
            this.value = value;
            this.target = target;
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            target.set((Integer) value.getValue());
        }
    }
    public class booleanHandler implements ActionListener{
        Boolean value;
        AtomicBoolean target;
        public booleanHandler(Boolean value,AtomicBoolean target){
            this.value = value;
            this.target = target;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            target.set(value);
        }
    }


}  