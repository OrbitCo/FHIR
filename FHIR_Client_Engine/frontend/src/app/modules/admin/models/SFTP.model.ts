

export interface SFTPModel {
    serverAddress: string,
    serverPort: string,
    targetDirectory: string,
    username: string,
    password: string,
    privateKeyBase64: string
}
