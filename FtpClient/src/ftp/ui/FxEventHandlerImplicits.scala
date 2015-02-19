package ftp.ui

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.event.ActionEvent

/**
 * This objects converts scala lambdas for the EventHandlers to "java-lambdas".
 */
object FxEventHandlerImplicits {
  implicit def mouseEvent2EventHandler(event:(MouseEvent)=>Any) = new EventHandler[MouseEvent]{
    override def handle(dEvent:MouseEvent):Unit = event(dEvent)
  }
  
  implicit def actionEvent2EventHandler(event:(ActionEvent)=>Any) = new EventHandler[ActionEvent]{
    override def handle(dEvent:ActionEvent):Unit = event(dEvent)
  }
}