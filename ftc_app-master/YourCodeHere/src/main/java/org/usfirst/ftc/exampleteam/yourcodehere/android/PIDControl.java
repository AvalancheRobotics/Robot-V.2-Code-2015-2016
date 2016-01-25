package org.usfirst.ftc.exampleteam.yourcodehere.android;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by Zach on 1/24/16.
 */
public class PIDControl {
    private double error;
    private double integral;
    private long lastTime;
    private double lastPos;
    private double derivative;
    private double kp;
    private double ki;
    private double kd;
    private DcMotor motor;

    public PIDControl(DcMotor m) {
        error = 0.0;
        integral = 0.0;
        lastTime = 0;
        lastPos = 0.0;
        derivative = 0.0;
        kp = 0.001;   //Temporary vars, test for actual vars
        ki = 0.0;
        kd = 0.0;
        motor = m;
    }

    public void setMotor(DcMotor m) {
        motor = m;
    }

    public void updatePower(long currentTime, int motorTargetPos, int motorCurrentPos) {   //Should be called in a loop
        if (motor == null) {
            throw new RuntimeException("Target motor is undeclared, please use .setMotor(DcMotor m) first");
        } else {
            error = motorTargetPos - motorCurrentPos; //error = position
            long dt;
            if (lastTime == 0.0) {
                dt = 0;
            } else {
                dt = currentTime - lastTime;
            }
            integral += error * dt;
            derivative = -(motorCurrentPos - lastPos) / dt;
            motor.setPower(kp * error + ki * integral + kd * derivative);
            lastTime = currentTime;
            lastPos = motorCurrentPos;
        }
    }

    public void reset() {
        error = 0.0;
        integral = 0.0;
        lastTime = 0;
        lastPos = 0.0;
        derivative = 0.0;
    }

    public void setConstants(double a, double b, double c) {
        kp = a;
        ki = b;
        kd = c;
    }
}