package com.macro.mall.common.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class ReactorStructureTest {

  private static final List<String> EXPECTED_MODULES =
      List.of(
          "mall-common",
          "mall-persistence",
          "mall-security",
          "mall-admin",
          "mall-portal",
          "mall-search");

  private static final Map<String, Set<String>> ALLOWED_INTERNAL_DEPENDENCIES =
      Map.of(
          "mall-common", Set.of(),
          "mall-persistence", Set.of("mall-common"),
          "mall-security", Set.of("mall-common"),
          "mall-admin", Set.of("mall-common", "mall-persistence", "mall-security"),
          "mall-portal", Set.of("mall-common", "mall-persistence", "mall-security"),
          "mall-search", Set.of("mall-common", "mall-persistence"));

  @Test
  void reactorContainsOnlyApprovedModulesAndDependencyEdges() throws Exception {
    Path repositoryRoot = findRepositoryRoot();
    Document rootPom = parse(repositoryRoot.resolve("pom.xml"));

    assertEquals(EXPECTED_MODULES, textValues(rootPom, projectPath("modules", "module")));
    assertTrue(
        textValues(rootPom, projectPath("dependencies", "dependency")).isEmpty(),
        "The parent must not leak dependencies into every child");

    for (String module : EXPECTED_MODULES) {
      Path modulePomPath = repositoryRoot.resolve(module).resolve("pom.xml");
      assertTrue(Files.isRegularFile(modulePomPath), "Missing child POM for " + module);
      Document modulePom = parse(modulePomPath);

      assertEquals(module, singleText(modulePom, projectPath("artifactId")));
      assertEquals("jar", singleText(modulePom, projectPath("packaging")));
      assertInternalDependenciesAreAllowed(module, modulePom);
    }

    assertNoSkippedTestsOrImagePlugins(repositoryRoot, rootPom);
  }

  private static void assertInternalDependenciesAreAllowed(String module, Document modulePom)
      throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList dependencies =
        (NodeList)
            xpath.evaluate(
                projectPath("dependencies", "dependency"), modulePom, XPathConstants.NODESET);

    for (int index = 0; index < dependencies.getLength(); index++) {
      Element dependency = (Element) dependencies.item(index);
      String groupId = childText(dependency, "groupId");
      if (!"com.macro.mall".equals(groupId)) {
        continue;
      }

      String artifactId = childText(dependency, "artifactId");
      assertTrue(
          ALLOWED_INTERNAL_DEPENDENCIES.get(module).contains(artifactId),
          () -> module + " has forbidden internal dependency on " + artifactId);
    }
  }

  private static void assertNoSkippedTestsOrImagePlugins(Path repositoryRoot, Document rootPom)
      throws IOException, XPathExpressionException {
    for (String module : EXPECTED_MODULES) {
      String pom = Files.readString(repositoryRoot.resolve(module).resolve("pom.xml"));
      assertFalse(pom.contains("<skipTests>"), module + " must not define skipTests");
      assertFalse(pom.contains("<maven.test.skip>"), module + " must not skip test compilation");
    }

    String rootPomText = Files.readString(repositoryRoot.resolve("pom.xml"));
    assertFalse(rootPomText.contains("<skipTests>"), "The parent must not define skipTests");
    assertFalse(
        rootPomText.contains("<maven.test.skip>"), "The parent must not skip test compilation");

    for (String plugin :
        textValues(rootPom, "//*[local-name()='plugin']/*[local-name()='artifactId']")) {
      String normalized = plugin.toLowerCase(Locale.ROOT);
      assertFalse(
          normalized.contains("docker")
              || normalized.contains("jib")
              || normalized.contains("fabric8")
              || normalized.contains("build-image"),
          "Image/deployment plugin is forbidden in the foundation lifecycle: " + plugin);
    }
  }

  private static Path findRepositoryRoot() throws IOException {
    Path candidate = Path.of("").toAbsolutePath().normalize();
    while (candidate != null) {
      Path pom = candidate.resolve("pom.xml");
      if (Files.isRegularFile(pom)) {
        String content = Files.readString(pom);
        if (content.contains("<artifactId>mall-parent</artifactId>")
            && content.contains("<modules>")) {
          return candidate;
        }
      }
      candidate = candidate.getParent();
    }
    throw new IOException("Unable to locate the mall-parent reactor root");
  }

  private static Document parse(Path pom)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    return factory.newDocumentBuilder().parse(pom.toFile());
  }

  private static List<String> textValues(Document document, String expression)
      throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
    List<String> values = new ArrayList<>(nodes.getLength());
    for (int index = 0; index < nodes.getLength(); index++) {
      values.add(nodes.item(index).getTextContent().trim());
    }
    return values;
  }

  private static String singleText(Document document, String expression)
      throws XPathExpressionException {
    List<String> values = textValues(document, expression);
    assertEquals(1, values.size(), "Expected exactly one value for " + expression);
    return values.get(0);
  }

  private static String childText(Element parent, String localName) {
    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE && localName.equals(child.getLocalName())) {
        String value = child.getTextContent();
        assertNotNull(value);
        return value.trim();
      }
    }
    return "";
  }

  private static String projectPath(String... elements) {
    StringBuilder path = new StringBuilder("/*[local-name()='project']");
    for (String element : elements) {
      path.append("/*[local-name()='").append(element).append("']");
    }
    return path.toString();
  }
}
