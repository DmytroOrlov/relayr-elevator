Elevator state is:
- Elevator ID
- current Floor Number
- direction (important to keep going same direction to prevent starvation and minimal one person traveling path)
- and simply Set of targets, as logic to chose next target depends on direction

Keep going same direction is better than FCFS because:
- for minimal one person traveling path
- any other optimisations can lead to starvation, so keep going same direction is better for one person pickup waiting time

Build:
```sh
$ sbt package
```
Run tests:
```sh
$ sbt test
```

Elevators state is not thread safe.

Elevators state is Map.empty[Int, ElevatorStatus]:
- key is id
- Map simplifies update of particular elevator state
- Map allow us to use any id we want
