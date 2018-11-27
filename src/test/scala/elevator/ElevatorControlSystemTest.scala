package elevator

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FlatSpec, MustMatchers}

class ElevatorControlSystemTest extends FlatSpec with MustMatchers with TypeCheckedTripleEquals {
  "status for new ElevatorControlSystem" should "be empty" in {
    val es = new Elevators()
    es.status() mustBe 'empty
  }
  "update" should "add new elevator" in {
    val es = new Elevators()
    es.update(0, 0, 1, Set.empty)
    es.status() must ===(Seq(ElevatorStatus(0, 0, 1, Set.empty)))
  }
  "maxNumber" should "limit elevator number" in {
    val es = new Elevators(1)
    es.update(0, 0, 1, Set.empty)
    an[IllegalArgumentException] must be thrownBy es.update(1, 0, 1, Set.empty)
  }
  "update" should "update existing elevator current floor" in {
    val es = new Elevators()
    es.update(0, 0, 1, Set.empty)
    es.update(0, 1, 1, Set.empty)
    es.status() must ===(Seq(ElevatorStatus(0, 1, 1, Set.empty)))
  }
  "update" should "update existing elevator direction" in {
    val es = new Elevators()
    es.update(0, 0, 1, Set.empty)
    es.update(0, 0, -1, Set.empty)
    es.status() must ===(Seq(ElevatorStatus(0, 0, -1, Set.empty)))
  }
  "update" should "update existing elevator target floors" in {
    val es = new Elevators()
    es.update(0, 0, 1, Set.empty)
    es.update(0, 0, 1, Set(1))
    es.status() must ===(Seq(ElevatorStatus(0, 0, 1, Set(1))))
  }
  "pickup" should "have direction" in {
    val es = new Elevators()
    an[IllegalArgumentException] must be thrownBy es.pickup(0, 0)
  }
  "pickup already targeted direction" should "wait to save machine hours of the rest elevators" in {
    val es = new Elevators()
    val target = 2
    es.update(0, curFloor = 0, 1, Set(target))
    es.update(1, curFloor = 1, 1, Set.empty)
    val prevState = es.status()

    es.pickup(floor = target, -1)
    es.status() must contain theSameElementsAs prevState
  }
  "if elevator already on target floor, pickup" should "choose one" in {
    val es = new Elevators()
    es.update(0, curFloor = 0, 1, Set.empty)
    es.update(1, curFloor = 1, 1, Set.empty)
    es.pickup(floor = 0, 1)
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 0, 1, Set(0)), ElevatorStatus(1, 1, 1, Set.empty))
  }
  "if elevator approaching and passing by, pickup" should "choose one to save machine hours of the rest" in {
    val es = new Elevators()
    es.update(0, curFloor = 0, 1, Set(4))
    es.update(1, curFloor = 2, 1, Set.empty)
    es.pickup(floor = 3, 1)
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 0, 1, Set(3, 4)), ElevatorStatus(1, 2, 1, Set.empty))
  }
  "if no passing by, pickup" should "choose closest empty" in {
    val es = new Elevators()
    es.update(0, curFloor = 0, 1, Set(4))
    es.update(1, curFloor = 3, 1, Set.empty)
    es.pickup(floor = 2, -1)
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 0, 1, Set(4)), ElevatorStatus(1, 3, 1, Set(2)))
  }
  "after all pickup" should "choose one with shortest path (finish current direction and return)" in {
    val es = new Elevators()
    es.update(0, curFloor = 1, -1, Set(0))
    es.update(1, curFloor = 3, 1, Set(5))
    es.pickup(floor = 2, -1)
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 1, -1, Set(0, 2)), ElevatorStatus(1, 3, 1, Set(5)))
  }
  "when target is approached, step" should "open door" in {
    val es = new Elevators()
    es.update(0, curFloor = 1, -1, Set(1))
    es.step()
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 1, -1, Set.empty))
  }
  "step" should "continue current up direction to prevent starvation and minimal one person traveling path" in {
    val es = new Elevators()
    es.update(0, curFloor = 1, 1, Set(0, 3))
    es.step()
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 2, 1, Set(0, 3)))
  }
  "step" should "continue current down direction to prevent starvation and minimal one person traveling path" in {
    val es = new Elevators()
    es.update(0, curFloor = 2, -1, Set(0, 3))
    es.step()
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 1, -1, Set(0, 3)))
  }
  "step" should "switch direction" in {
    val es = new Elevators()
    es.update(0, curFloor = 1, 1, Set(0))
    es.step()
    es.status must contain theSameElementsAs Seq(ElevatorStatus(0, 1, -1, Set(0)))
  }
}
