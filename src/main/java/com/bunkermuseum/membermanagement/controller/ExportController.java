package com.bunkermuseum.membermanagement.controller;

public class ExportController {
    private final UserController userController;
    private final BookingController bookingController;
    private final EmailController emailController;

    public ExportController(
            UserController userController,
            BookingController bookingController,
            EmailController emailController
    ) {
        this.userController = userController;
        this.bookingController = bookingController;
        this.emailController = emailController;
    }

}