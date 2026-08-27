@Grab(group='com.stimulsoft', module='stimulsoft-reports-webdesigner', version='2026.3.2')
@Grab(group='com.stimulsoft', module='stimulsoft-reports-web', version='2026.3.2')
@Grab(group='com.stimulsoft', module='stimulsoft-reports-base', version='2026.3.2')
@Grab(group='com.stimulsoft', module='stimulsoft-reports-report', version='2026.3.2')

import com.stimulsoft.webdesigner.StiWebDesigner;
import com.stimulsoft.webviewer.StiWebViewer;

println "Designer Handler: " + StiWebDesigner.SESSION_ATTRIBUTE_HANDLER;
println "Viewer Parameters: " + StiWebViewer.SESSION_ATTRIBUTE_PARAMETERS;
