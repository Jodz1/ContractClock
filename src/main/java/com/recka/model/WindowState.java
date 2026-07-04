package com.recka.model;

public class WindowState {
    private Long id;
    private String windowName;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private boolean maximized;
    private Long lastOpenedContractId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWindowName() { return windowName; }
    public void setWindowName(String windowName) { this.windowName = windowName; }
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
    public Double getWidth() { return width; }
    public void setWidth(Double width) { this.width = width; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public boolean isMaximized() { return maximized; }
    public void setMaximized(boolean maximized) { this.maximized = maximized; }
    public Long getLastOpenedContractId() { return lastOpenedContractId; }
    public void setLastOpenedContractId(Long lastOpenedContractId) { this.lastOpenedContractId = lastOpenedContractId; }
}
