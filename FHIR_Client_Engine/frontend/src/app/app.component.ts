import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    /**
     * Constructor
     */
    constructor(private route: ActivatedRoute
    ) {
    }
    ngOnInit(): void {
       if(this.extractCode('code', window.location.href)) {
        this.postCode(this.extractCode('code', window.location.href));
       }
    }

    extractCode(field: any, url: string) : string {
        const windowLocationUrl = url;
        const reg = new RegExp('[?&]' + field + '=([^&#]*)', 'i');
        const string = reg.exec(windowLocationUrl);
        return string ? string[1] : null;
    }

    postCode(code) {
        window.localStorage.setItem('code', code);
        window.close();
    }
}
