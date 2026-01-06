package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.User
import javafx.fxml.FXML
import javafx.scene.image.{Image, ImageView}

class HeaderController {

  // FXML UI element injection
  @FXML
  private var userProfileImageView: ImageView = _

  // Initializes the controller with the current user's data
  def initData(user: User): Unit = {
    user.imagePath.foreach { path =>
      try {
        val userImage = new Image(getClass.getResourceAsStream(path))
        if (!userImage.isError) {
          userProfileImageView.setImage(userImage)
        }
      } catch {
        case e: Exception => println(s"HeaderController: Error loading image resource: $path")
      }
    }
  }

  // Handles the click event for the user's profile image
  @FXML
  private def handleProfileClick(): Unit = {
    MainApp.showProfileView()
  }
}