import java.util.List;

public class MyDispatcher extends Dispatcher {

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }
    private int lastAssignedNode = 0;
    @Override
    public synchronized void addTask(Task task) {
        switch(algorithm) {
            case ROUND_ROBIN:
                preemptibleTask(task, lastAssignedNode);
                lastAssignedNode = (lastAssignedNode + 1) % hosts.size();
                break;
            case  SHORTEST_QUEUE:
                int indexMinQueue = 0;
                int minSize = hosts.get(0).getQueueSize();

                for(int i = 1; i < hosts.size(); i++) {
                    if(minSize > hosts.get(i).getQueueSize()) {
                        minSize = hosts.get(i).getQueueSize();
                        indexMinQueue = i;
                    }
                }

                preemptibleTask(task, indexMinQueue);
                break;
            case  SIZE_INTERVAL_TASK_ASSIGNMENT:
                int index = -1;

                if(task.getType() == TaskType.SHORT) {
                    index = 0;
                } else if(task.getType() == TaskType.MEDIUM) {
                    index = 1;
                } else if(task.getType() == TaskType.LONG) {
                    index = 2;
                }

                preemptibleTask(task, index);
                break;
            case LEAST_WORK_LEFT:
                indexMinQueue = 0;
                long minWorkLeft = hosts.get(0).getWorkLeft();

                for(int i = 1; i < hosts.size(); i++) {
                    if( minWorkLeft > hosts.get(i).getWorkLeft()) {
                        minWorkLeft = hosts.get(i).getWorkLeft();
                        indexMinQueue = i;
                    }
                }

                preemptibleTask(task, indexMinQueue);

                break;
            default:
                break;
        }
    }

    private void preemptibleTask(Task task, int index) {
        Host currentHost = hosts.get(index);
        Task currentTask = ((MyHost) hosts.get(index)).getCurrentTask();
        if(currentTask != null) {
            if(currentTask.isPreemptible() && currentTask.getPriority() < task.getPriority()) {
                ((MyHost) currentHost).preemptCurrentTask();
                currentHost.addTask(task);
                currentHost.addTask(currentTask);
            } else {
                currentHost.addTask(task);
            }
        } else {
            currentHost.addTask(task);
        }
    }
}
