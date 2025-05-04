import {Component, Input} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {Heartbeat} from '../../heartbeat';

@Component({
  selector: 'app-heartbeat',
  imports: [MatButtonModule, MatCardModule],
  templateUrl: './heartbeat.component.html',
  styleUrl: './heartbeat.component.scss',
  standalone : true
})
export class HeartbeatComponent {
  @Input() heartbeat: Heartbeat = new Heartbeat(0, '');
}
