package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;
import java.net.URL;
import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.Downloader;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.PhantomJSGitHubBitbucketSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

/**
 * A class for handling PhantomJS binaries
 * <br/>
 * <b>Not fully implemented - downloading is not supported using an {@link ExternalBinarySource}</b>
 */
public class PhantomJSDriverBinaryHandler extends AbstractBinaryHandler {

    public static final String PHANTOMJS_BINARY_VERSION_PROPERTY = "phantomjsBinaryVersion";
    public static final String PHANTOMJS_BINARY_URL_PROPERTY = "phantomjsBinaryUrl";
    public static final String PHANTOMJS_BINARY_PROPERTY = "phantomjsBinary";

    public static final String PHANTOMJS_BINARY_NAME = "phantomjs" + (PlatformUtils.isWindows() ? ".exe" : "");

    private DesiredCapabilities capabilities;

    public PhantomJSDriverBinaryHandler(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Takes care of all steps but the first one of the method {@link AbstractBinaryHandler#downloadAndPrepare()}
     *
     * @param targetDir
     *     A directory where a downloaded binary should be stored
     * @param from
     *     A url a binary should be downloaded from
     *
     * @return An executable binary that was extracted/copied from the downloaded file
     *
     * @throws Exception
     *     If anything bad happens
     */
    protected File downloadAndPrepare(File targetDir, URL from) throws Exception {
        File downloaded = Downloader.download(targetDir, from);
        File extraction = BinaryFilesUtils.extract(downloaded);

        File[] phantomJSDirectory = extraction.listFiles(file -> file.isDirectory());
        if (phantomJSDirectory == null || phantomJSDirectory.length == 0) {
            throw new IllegalStateException(
                "The extracted phantomJS directory directory is missing at the location: " + extraction
                    + " - the  number of contained directories is 0");
        }

        File binDir = new File(phantomJSDirectory[0], "bin");
        File[] files = binDir.listFiles(file -> file.isFile() && file.getName().equals(PHANTOMJS_BINARY_NAME));

        if (files == null || files.length == 0) {
            throw new IllegalStateException(
                "The phantomJS binary is not present on the expected path " + new File(binDir, PHANTOMJS_BINARY_NAME));
        }

        return markAsExecutable(files[0]);
    }

    @Override
    protected String getBinaryProperty() {
        return PHANTOMJS_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return PHANTOMJS_EXECUTABLE_PATH_PROPERTY;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.PhantomJS().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return PHANTOMJS_BINARY_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return PHANTOMJS_BINARY_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new PhantomJSGitHubBitbucketSource(new HttpClient(), new GitHubLastUpdateCache());
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }
}
