import java.net.*;

public class ProxyAcceptCookiePolicy implements CookiePolicy {
    private String acceptedProxy;

    public ProxyAcceptCookiePolicy(String acceptedProxy) {
        this.acceptedProxy = acceptedProxy;
    }

    public boolean shouldAccept(URI uri, HttpCookie cookie) {
        String host = null;
        try {
            host = InetAddress.getByName(uri.getHost()).getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (HttpCookie.domainMatches(acceptedProxy, host)) {
            return true;
        }

        return CookiePolicy.ACCEPT_ORIGINAL_SERVER
                .shouldAccept(uri, cookie);
    }

    // standard constructors
}