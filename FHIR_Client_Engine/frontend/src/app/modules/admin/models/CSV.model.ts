import {EmailModel} from "./email.model";
import {SFTPModel} from "./SFTP.model";


export interface CSVModel {
    columns: string[],
    selectedColumns: string[],
    fileName: string,
    body: string; //Bundle to make a CSV from (typically a search result)
    emailExport: EmailModel; //Null if email is not specified
    SFTPExport: SFTPModel; //Null if SFTP is not specified
}
