package io.cloudslang.worker.execution.services;

public class StubConnectionStateImpl implements RobotConnectionState {
    @Override
    public boolean hasRunningRobot(String group) {
        return false;
    }
}
