import {Injectable} from '@angular/core';
import {AppConfig} from '../models/app.config';
import {HttpClient} from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class AppConfigService extends AppConfig {
  constructor(private _http: HttpClient) {
    super();
  }
  getAppConfig() {
    let host;
    let port;
    return this._http.get('assets/reindex.yml', {
      observe: 'body',
      responseType: 'text'   // This one here tells HttpClient to parse it as text, not as JSON
    })
      .toPromise()
      .then(data => {
        const result = data.trim().split('\n');
        result.forEach((item) => {
          if (item.includes('host')) {
            host = result[0].split(':')[1].trim();
          } else if (item.includes('port')) {
            port = result[1].split(':')[1].trim();
          }
        });
        this.serverBaseUrl = `http://${host}:${port}`;
        this.localBaseUrl = this.serverBaseUrl;
      });
  }

}
