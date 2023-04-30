import {FHIRExportModel} from "./FHIRExport.model";
import {CSVModel} from "./CSV.model";

export interface ExportModel {
    FHIRExport: FHIRExportModel; //Null if not selected
    CSVExport: CSVModel;
    //JSONExport: JSONModel;
}
