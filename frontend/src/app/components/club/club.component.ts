import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ClubService} from '../../services/club/club.service';
import {Club} from '../../models/club.model';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-club',
  templateUrl: './club.component.html',
  standalone: true,
  imports: [
    NgIf
  ],
  styleUrls: ['./club.component.css']
})
export class ClubComponent implements OnInit {
  public club: Club | undefined;

  constructor(
    private route: ActivatedRoute,
    private clubService: ClubService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const clubId = +params['id'];
      this.loadClub(clubId);
    });
  }

  loadClub(clubId: number) {
    this.clubService.getClubById(clubId).subscribe((club: Club) => {
      this.club = club;
    });
  }

  goBack() {
    this.router.navigate(['/clubs']);
  }
}
