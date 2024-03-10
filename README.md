# Copyright Nioata Alexandra 332CA 2023

Tema 2 APD

Scopul temei este implementarea in Java a unui scheduler, folosind Java Threads. Mai exact, trebuie completate clasele MyHost.java si MyDispacther.java.
Dispatcherul se ocupa de preluarea task-urilor si de trimiterea acestora la hosturi(pe baza unor politici de planificare, adica cei 4 algoritmi: Round Robin, Shortest Queue, Size Interval Task Assignment, Least Work Left explicati in enunt), in timp ce rolul unui Host este de a executa task-ul trimis de dispatcher. 

MyDispatcher:

Functii folosite:
- addTask: - adauga un nou task in sistemul de planificare
           - selecteaza unul dintre cei 4 algoritmi de planificare
           - metoda sincronizata (este folosit cuvantul synchronized) pentru ca aceasta sa fie accesata la un moment dat doar de un singur fir de executie
           - se foloseste un switch-case pentru a se executa algoritmul data de catre enumul algorithm
           - ROUND_ROBIN: selecteaza nodul urmator tinand cont de variabila lastAssignedNode, adaugand task-ul pe acel nod si actualizand variabila (initial lastAssignedNode e 0)
           - SHORTEST_QUEUE: adauga task-ul in coada care are marime minima, tinand cont si daca in hostul respectiv un task este in executie sau nu
           - SIZE_INTERVAL_TASK_ASSIGNMENT: determina tipul task-ului (short, medium, long) si alege nodul in functie de acest tip
           - LEAST_WORK_LEFT: gaseste nodul cu cel mai mic timp de calcul ramas (tine cont si daca un task e in executie sau nu) si adauga task-ul

- preemptibleTask: - folosita pentru implementarea functionalitatii de preemtare, care se poate realizare doar daca task-ul aflat in executie e preemptibil si daca dispatcherul
                        asigneaza un task cu prioritate mai mare
                   - daca poate avea loc aceasta functionalitate, se opreste executia task-ului curent folosind preemptCurrentTask() din MyHost, apoi se adauga task-ul nou, iar apoi
                   din nou task-ul care a fost oprit, in caz contrar se adauga normal task-ul in hostul ales de catre dispacther


MyHost.java:

Variabile adaugate:
- queue: coada de prioritati care contine obiecte de tip Task, stocheaza task-urile in functie de prioritatea lor folosind un comparator,
                are o valoare initiala de 100 (a fost data aleator, deoarece clasa PriorityBlockingQueue cere valoare intiala)

- semaphore: lock folosit pentru sincronizarea între firele de execuție,este intializat cu 0, pentru a bloca firul de execuție până când este eliberat explicit pentru a permite continuarea,
                folosit atunci cand un task este trimis in host si acesta trebuie executat

- isRunning: boolean folosit in metoda run() pentru a vedea daca firul de executie e activ sau nu, o data ce toate task-urile au fost executate, acesta va primi false

- currentTask: referinta la task-ul curent care este executat

- isInterrupted: boolean folosit pentru a vedea daca task-ul curent e intrerupt sau nu, folosit pentru a implementa functionalitate de preemptare a task-urilor

- totalWorkLeft: obiect de tip Atomic Long care stocheaza cantitatea de munca ramasa de efectuat de catre host, actualizeaza si urmareste timpul ramas de munca 
                 in mod sigur si atomic in cadrul operatiilor concurente.

- isExecuted: indica daca un task a fost executat sau nu, folosit pentru a gestiona starea de executie a task-ului in momentul preluarii si executiei din coada.

Clasa adaugata:
- TaskPriorityComparator: clasa static ce implementeaza Comparator<Task>, ordoneaza task-urile in functie de prioritate (daca un task are prioritatea mea mare, acesta
                            va intra in coada inainte celui cu prioritate mai mica), daca prioritatile sunt egale se adauga in functie de timpul de start (crescator)
                        si este folosita pentru queue, care e o coada de prioritati
 
Functii folosite:
- run: - executarea efectiva a nodului
       - se foloseste de variabila isRunning care e setata pe True, adica firul de executie functioneaza
       - incearca sa obtina un semnal de la semafor
       - extrage un task din coada de prioritati si se initializeaza cu true variabila isExecuted, se foloseste un while-loop care ruleaza pana cand timpul ramas al taskului 
        e 0 (pentru a consuma executia unui task am folosit Thread.sleep pentru 1000 de milisecunde, adica o secunda), actualizand si timpul ramas al task-ului (task.getLeft), dar
        si variabila totalWorkLeft (se scad cele 1000 de milisecunde)
        - daca in timpul executiei task-ului, variabila inInterrupted devine true, inseamna ca va avea loc preemptarea iar loop-ul ete intrerupt folosind un break
        - la final se finalizeaza task-ului prin metoda finish(), astfel aflandu-se timpul de finish al fiecarui task in parte
        - la finalul executiei task-ului, isExecuted devine fals

- addTask: - adauga task nou in coada de prioritati a hostului
            - actualizeaza variabila totalWorkLeft adaugand durata task-ului
            - semnalul semaforului e eliberat pentru a anunta disponibilitatea unui nou task in coada 
           
- getQueueSize: - returneaza dimensiunea cozii de task-uri, in functie de valoarea booleanului isExecuted (daca acesta e true) i se adauga o valoare la 
                dimensiunea cozii, pentru a simula o valoare mai mare in cazul in care un task e executat la acel moment

- getWorkLeft: - returneaza durata totala de calcule ramase pentru nodul respectiv, in functie de valoarea booleanului isExecuted (daca acesta e true) i se adauga o valoare la 
                dimensiunea cozii, pentru a simula o valoare mai mare in cazul in care un task e executat la acel moment

- shutdown: - seteaza variabila isRunnig cu false, pentru a rata ca nodul trebuie oprit
            - foloseste interrupt() pentru a intrerupe nodul

- getCurrentTask: - folosit in MyDispatcher pentru implementarea preemptarii
                  - returneaza task-ul aflat in executie

- preemptCurrentTask: - folosit in MyDispatcher pentru implementarea preemptarii
                      - actualizeaza variabila isInterrupted pentru a indica faptul ca task-ul curent trebuie oprit, pentru a fi executat task-ul cu prioritatea mai mare



Resurse:
1. laboratorul 4 (https://mobylab.docs.crescdi.pub.ro/docs/parallelAndDistributed/laboratory4)
2. laboratorul 5 (https://mobylab.docs.crescdi.pub.ro/docs/parallelAndDistributed/laboratory5/semaphores)
3. laboratorul 6 (https://mobylab.docs.crescdi.pub.ro/docs/parallelAndDistributed/laboratory6/)