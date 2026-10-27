package org.keycloak.services;

import java.net.URI;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class UrlsStaticIssuerTest {

    @Test
    public void emptyConfigHasNoStaticIssuers() {
        Assert.assertTrue(Urls.parseStaticIssuers(null).isEmpty());
        Assert.assertTrue(Urls.parseStaticIssuers("  ").isEmpty());
    }

    @Test
    public void parsesRealmsAndStripsTrailingSlash() {
        Map<String, URI> issuers = Urls.parseStaticIssuers(
                " privata-vpn-dev=https://auth.dev.privata-solutions.com/auth/ , strata=https://auth.strata.example/auth,");
        Assert.assertEquals(URI.create("https://auth.dev.privata-solutions.com/auth"), issuers.get("privata-vpn-dev"));
        Assert.assertEquals(URI.create("https://auth.strata.example/auth"), issuers.get("strata"));
        Assert.assertEquals(2, issuers.size());
    }

    @Test
    public void staticBaseProducesSameIssuerShapeAsRequestBase() {
        String fromRequest = Urls.realmBase(URI.create("https://auth.dev.privata-solutions.com/auth/"))
                .path("{realm}").build("privata-vpn-dev").toString();
        String fromStatic = Urls.realmBase(Urls.parseStaticIssuers("r=https://auth.dev.privata-solutions.com/auth").get("r"))
                .path("{realm}").build("privata-vpn-dev").toString();
        Assert.assertEquals("https://auth.dev.privata-solutions.com/auth/realms/privata-vpn-dev", fromRequest);
        Assert.assertEquals(fromRequest, fromStatic);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsEntryWithoutBaseUrl() {
        Urls.parseStaticIssuers("privata-vpn-dev=");
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsEntryWithoutRealm() {
        Urls.parseStaticIssuers("=https://auth.example/auth");
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsNonHttpBaseUrl() {
        Urls.parseStaticIssuers("privata-vpn-dev=auth.example/auth");
    }

    @Test
    public void unconfiguredRealmKeepsRequestBasedIssuer() {
        Assert.assertFalse(Urls.hasStaticIssuer("master"));
        Assert.assertEquals("https://alias.example/auth/realms/master",
                Urls.realmIssuer(URI.create("https://alias.example/auth/"), "master"));
    }
}
