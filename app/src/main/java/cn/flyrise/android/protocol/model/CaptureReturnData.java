package cn.flyrise.android.protocol.model;

/**
 * 二维码扫描返回的数据
 */
public class CaptureReturnData {
    private String ip;

    private String port;

    private boolean isHttps;

    private boolean isOpenVpn;

    private String vpnAddress;

    private String vpnPort;

    private String vpnName;

    private String vpnPassword;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public void setHttps(boolean https) {
        isHttps = https;
    }

    public boolean isOpenVpn() {
        return isOpenVpn;
    }

    public void setOpenVpn(boolean openVpn) {
        isOpenVpn = openVpn;
    }

    public String getVpnAddress() {
        return vpnAddress;
    }

    public void setVpnAddress(String vpnAddress) {
        this.vpnAddress = vpnAddress;
    }

    public String getVpnPort() {
        return vpnPort;
    }

    public void setVpnPort(String vpnPort) {
        this.vpnPort = vpnPort;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }

    public String getVpnPassword() {
        return vpnPassword;
    }

    public void setVpnPassword(String vpnPassword) {
        this.vpnPassword = vpnPassword;
    }
}
