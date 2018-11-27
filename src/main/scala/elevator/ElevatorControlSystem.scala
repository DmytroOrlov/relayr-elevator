package elevator

trait ElevatorControlSystem {
  def status(): Seq[ElevatorStatus]

  def update(id: Int, curFloor: Int, direction: Int, targetFloors: Set[Int])

  def pickup(floor: Int, direction: Int)

  def step()
}

/**
  * @param direction    important to keep going same direction to prevent starvation
  *                     and minimal one person traveling path
  * @param targetFloors Set of targets, logic to chose next depends on direction
  */
case class ElevatorStatus(id: Int, curFloor: Int, direction: Int, targetFloors: Set[Int]) {
  require(direction != 0, "direction must be negative for down, positive for up")
}

/**
  * Elevators number is up to 16 by default.
  * State is not thread safe.
  */
class Elevators(maxNumber: Int = 16) extends ElevatorControlSystem {
  private var elevators = Map.empty[Int, ElevatorStatus]

  override def status(): Seq[ElevatorStatus] = elevators.values.toSeq

  /**
    * Can violate state and lead to elevators "teleportation" :)
    */
  override def update(id: Int, curFloor: Int, direction: Int, targetFloors: Set[Int]): Unit = {
    val es = elevators + (id -> ElevatorStatus(id, curFloor, direction, targetFloors))
    require(es.size <= maxNumber, s"elevator control system able to handle up to $maxNumber elevators")
    elevators = es
  }

  /**
    * Targets particular elevator to new floor.
    * Note 1: a.direction * b.direction > 0 - directions are the same
    * Note 2: e.targetFloors.isEmpty - empty elevator.
    */
  override def pickup(floor: Int, direction: Int): Unit = {
    require(direction != 0, "must be negative for down, positive for up")
    // elevator already targeted thereto than just wait to save machine hours of the rest elevators
    if (!elevators.values.exists(_.targetFloors.contains(floor))) {
      // otherwise check if elevator already on target floor
      val alreadyHere = elevators.values.collectFirst {
        case el if el.curFloor == floor => el
      }
      val e = alreadyHere.getOrElse {
        // else choose approaching and passing by (to save machine hours of the rest)
        val sameDirection = elevators.values.filter(e => e.targetFloors.nonEmpty && e.direction * direction > 0)
        val approachingAndPassingBy = sameDirection.filter(e =>
          direction > 0 && floor > e.curFloor && floor < e.targetFloors.max ||
            direction < 0 && floor < e.curFloor && floor > e.targetFloors.min
        )
        if (approachingAndPassingBy.nonEmpty) {
          val closest = approachingAndPassingBy.minBy(e => (e.curFloor - floor).abs)
          closest
        } else {
          // else choose closest empty elevator
          val emptyEs = elevators.values.filter(_.targetFloors.isEmpty)
          if (emptyEs.nonEmpty) {
            val closestEmpty = emptyEs.minBy(e => (e.curFloor - floor).abs)
            closestEmpty
          } else {
            // after all choose one with shortest path (finish current direction and return)
            elevators.values.minBy { e =>
              val currentDirectionLastStop =
                if (e.direction > 0) e.targetFloors.max else e.targetFloors.min
              val currentDirectionPath =
                (e.curFloor - currentDirectionLastStop).abs
              val returnPath = (floor - currentDirectionLastStop).abs
              currentDirectionPath + returnPath
            }
          }
        }
      }
      elevators += (e.id -> e.copy(targetFloors = e.targetFloors + floor))
    }
  }

  override def step(): Unit = {
    elevators = elevators.mapValues { e =>
      // approached next target floor, can open door
      if (e.targetFloors.contains(e.curFloor))
        e.copy(targetFloors = e.targetFloors - e.curFloor)
      // continue current up direction
      else if (e.direction > 0 && e.targetFloors.exists(_ > e.curFloor))
        e.copy(curFloor = e.curFloor + 1)
      // continue current down direction
      else if (e.direction < 0 && e.targetFloors.exists(_ < e.curFloor))
        e.copy(curFloor = e.curFloor - 1)
      // switch direction
      else if (e.targetFloors.nonEmpty)
        e.copy(direction = -e.direction)
      // otherwise keep old state
      else e
    }
  }
}
