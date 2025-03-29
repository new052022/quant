package com.market.commander.quant.enums;

public enum OrderStatus {

    NEW,                    // New order
    OPEN,                   // Open
    INSUFFICIENT_FUNDS,     // Insufficient funds
    POSITION_LIMIT_EXCEEDED, // Exceeded max position limit for the symbol
    COMPLETED,              // Completed
    CANCELLED,              // Cancelled
    PENDING,                // Pending
    FAILED;                 // Error during processing

    // Method to get the human-readable label for the status
    public String getStatusLabel() {
        return switch (this) {
            case NEW -> "New";
            case OPEN -> "Open";
            case INSUFFICIENT_FUNDS -> "Insufficient funds";
            case POSITION_LIMIT_EXCEEDED -> "Too much open positions/orders by this symbol";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
            case PENDING -> "Pending";
            case FAILED -> "Error";
        };
    }

}
