package ch.makery.address

import ch.makery.address.controller._
import ch.makery.address.model.{FoodBank, User}
import ch.makery.address.util.Database
import javafx.fxml.FXMLLoader
import javafx.scene.image.Image
import javafx.scene.layout.{BorderPane, StackPane}
import javafx.scene.{Parent => jfxsParent}
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene

import scala.sys.exit

object MainApp extends JFXApp3 {

  // --- Application Properties ---
  private val AppWidth = 1000.0
  private val AppHeight = 600.0
  var currentUser: Option[User] = None
  val css: java.net.URL = getClass.getResource("/ch/makery/address/view/style.css")
  var viewHistory: List[() => Unit] = List()

  // --- Application Lifecycle ---

  override def main(args: Array[String]): Unit = {
    if (args.headOption.contains("--setup-db")) {
      println("Running initial database setup and seeding...")
      Database.setup()
      println("Setup complete. The application will now exit.")
      exit(0)
    } else {
      super.main(args)
    }
  }

  override def start(): Unit = {
    Database.setup()

    stage = new PrimaryStage {
      title = "Food For All"
      resizable = true
      icons += new Image(getClass.getResourceAsStream("/image/logo.png"))
      scene = new Scene(width = AppWidth, height = AppHeight) {
        if (css != null) stylesheets = Seq(css.toExternalForm)
      }
    }
    showLoginView()
    stage.show()
  }

  // --- Core View Loading Logic ---

  /**
   * The single, robust method for loading a view into the main application frame.
   * It loads the content, header, and hamburger menu, layering them in a StackPane.
   * @return The FXMLLoader for the content pane, allowing access to its controller.
   */
  private def showInMainFrame(contentFxmlPath: String, user: User): FXMLLoader = {
    val contentResource = getClass.getResource(contentFxmlPath)
    if (contentResource == null) throw new java.io.FileNotFoundException(s"Cannot find FXML: $contentFxmlPath")
    val contentLoader = new FXMLLoader(contentResource)
    val contentRoot: jfxsParent = contentLoader.load()

    val headerResource = getClass.getResource("/ch/makery/address/view/Header.fxml")
    if (headerResource == null) throw new java.io.FileNotFoundException("Cannot find Header.fxml")
    val headerLoader = new FXMLLoader(headerResource)
    val headerRoot: jfxsParent = headerLoader.load()
    val headerController = headerLoader.getController[HeaderController]
    headerController.initData(user)

    val hamburgerResource = getClass.getResource("/ch/makery/address/view/HamburgerMenu.fxml")
    if (hamburgerResource == null) throw new java.io.FileNotFoundException("Cannot find HamburgerMenu.fxml")
    val hamburgerLoader = new FXMLLoader(hamburgerResource)
    val hamburgerRoot: jfxsParent = hamburgerLoader.load()
    val hamburgerController = hamburgerLoader.getController[HamburgerMenuController]
    hamburgerController.setUser(user)

    val mainLayout = new BorderPane()
    mainLayout.setTop(headerRoot)
    mainLayout.setCenter(contentRoot)

    val finalRoot = new StackPane()
    finalRoot.getChildren.addAll(mainLayout, hamburgerRoot)
    javafx.scene.layout.StackPane.setAlignment(hamburgerRoot, javafx.geometry.Pos.TOP_RIGHT)

    stage.scene().setRoot(finalRoot)

    contentLoader
  }

  // --- Standalone Views (Login/Register) ---

  def showLoginView(): Unit = {
    currentUser = None
    viewHistory = List()
    val loader = new FXMLLoader(getClass.getResource("/ch/makery/address/view/Login.fxml"))
    stage.scene().setRoot(loader.load())
    stage.title = "Food For All | Login"
  }

  def showRegisterView(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/ch/makery/address/view/register.fxml"))
    stage.scene().setRoot(loader.load())
    stage.title = "Register New Account"
  }

  // --- Navigation History ---

  def navigateBack(): Unit = {
    viewHistory match {
      case head :: tail =>
        viewHistory = tail
        head()
      case Nil =>
        println("Warning: navigateBack called, but history is empty.")
    }
  }

  // ========================================================================
  // --- APPLICATION NAVIGATION METHODS ---
  // ========================================================================

  // --- Main Dashboards ---

  def showMainView(user: User): Unit = {
    currentUser = Some(user)
    stage.title = "Food for All"
    viewHistory = List()
    showInMainFrame("/ch/makery/address/view/UserMainView.fxml", user)
  }

  def showAdminMainView(user: User): Unit = {
    currentUser = Some(user)
    stage.title = "Food for All [Admin Dashboard]"
    viewHistory = List()
    showInMainFrame("/ch/makery/address/view/AdminMainView.fxml", user)
  }

  // --- User-Specific Views ---

  def showUserFoodBankListView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Our Food Banks"
      viewHistory = (() => showMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/UserFoodBankView.fxml", user)
    }
  }

  def showUserFoodBankDetailView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = foodBank.name
      viewHistory = (() => showUserFoodBankListView()) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/UserFoodBankDetailView.fxml", user)
      val controller = loader.getController[UserFoodBankDetailController]
      controller.initData(foodBank)
    }
  }

  def showUserStatusView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "My Activity Status"
      viewHistory = (() => showMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/UserStatusView.fxml", user)
    }
  }

  // --- Form Views (User) ---

  def showGeneralDonationForm(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Pledge a Donation"
      viewHistory = (() => showMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/UserDonationFormView.fxml", user)
    }
  }

  def showDonationFormView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Donate to ${foodBank.name}"
      viewHistory = (() => showUserFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/UserDonationFormView.fxml", user)
      val controller = loader.getController[UserDonationFormController]
      controller.initData(foodBank)
    }
  }

  def showGeneralVolunteerView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Volunteer Sign-Up"
      viewHistory = (() => showMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/UserVolunteerView.fxml", user)
    }
  }

  def showUserVolunteerView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Volunteer at ${foodBank.name}"
      viewHistory = (() => showUserFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/UserVolunteerView.fxml", user)
      val controller = loader.getController[UserVolunteerController]
      controller.initData(foodBank)
    }
  }

  def showGeneralRequestForm(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Request a Donation"
      viewHistory = (() => showMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/UserRequestDonationView.fxml", user)
    }
  }

  def showRequestDonationView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Request from ${foodBank.name}"
      viewHistory = (() => showUserFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/UserRequestDonationView.fxml", user)
      val controller = loader.getController[UserRequestDonationController]
      controller.initData(foodBank)
    }
  }

  // --- Admin-Specific Views ---

  def showFoodBankListView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Food Bank List"
      viewHistory = (() => showAdminMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/AdminFoodBankView.fxml", user)
    }
  }

  def showFoodBankDetailView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = foodBank.name
      viewHistory = (() => showFoodBankListView()) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/AdminFoodBankDetailView.fxml", user)
      val controller = loader.getController[AdminFoodBankDetailController]
      controller.initData(foodBank)
    }
  }

  def showAddFoodBankView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Add New Food Bank"
      viewHistory = (() => showFoodBankListView()) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/AdminEditFoodBankInfo.fxml", user)
      val controller = loader.getController[AdminEditFoodBankController]
      controller.initData(None)
    }
  }

  def showEditFoodBankView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Edit: ${foodBank.name}"
      viewHistory = (() => showFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/AdminEditFoodBankInfo.fxml", user)
      val controller = loader.getController[AdminEditFoodBankController]
      controller.initData(Some(foodBank))
    }
  }
  
  def showNewAdminView(): Unit = {
    currentUser.foreach { user =>
      if (user.role.equalsIgnoreCase("superadmin") || user.role.equalsIgnoreCase("admin")) {
        stage.title = "Create New Admin"
        viewHistory = (() => showAdminMainView(user)) :: viewHistory
        showInMainFrame("/ch/makery/address/view/NewAdminView.fxml", user)
      }
    }
  }

  def showGeneralActiveRequestsView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Active Requests"
      viewHistory = (() => showAdminMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/AdminActiveRequestsView.fxml", user)
    }
  }

  def showActiveRequestsView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Active Requests for ${foodBank.name}"
      viewHistory = (() => showFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/AdminActiveRequestsView.fxml", user)
      val controller = loader.getController[AdminActiveRequestsController]
      controller.initData(foodBank)
    }
  }

  def showGeneralVolunteerManagementView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "Volunteer Management"
      viewHistory = (() => showAdminMainView(user)) :: viewHistory
      showInMainFrame("/ch/makery/address/view/AdminVolunteerManagementView.fxml", user)
    }
  }

  def showVolunteerManagementView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Volunteer Schedule for ${foodBank.name}"
      viewHistory = (() => showFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/ADminVolunteerManagementView.fxml", user)
      val controller = loader.getController[AdminVolunteerManagementController]
      controller.initData(foodBank)
    }
  }

  def showInventoryManagementView(foodBank: FoodBank): Unit = {
    currentUser.foreach { user =>
      stage.title = s"Inventory for ${foodBank.name}"
      viewHistory = (() => showFoodBankDetailView(foodBank)) :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/AdminInventoryManagementView.fxml", user)
      val controller = loader.getController[AdminInventoryManagementController]
      controller.initData(foodBank)
    }
  }

  def showProfileView(): Unit = {
    currentUser.foreach { user =>
      stage.title = "User Profile"
      val backAction = if (user.role.equalsIgnoreCase("admin") || user.role.equalsIgnoreCase("superadmin")) {
        () => showAdminMainView(user)
      } else {
        () => showMainView(user)
      }
      viewHistory = backAction :: viewHistory
      val loader = showInMainFrame("/ch/makery/address/view/Profile.fxml", user)
      val profileController = loader.getController[ProfileViewController]
      profileController.initData(user)
    }
  }
}